package org.commonjava.maven.galley.cache.infinispan;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.cdi.util.weft.WeftManaged;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.util.PathUtils;
import org.infinispan.Cache;
import org.infinispan.cdi.ConfigureCache;
import org.infinispan.commons.util.concurrent.ConcurrentWeakKeyHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * This cache provider provides the ability to write the backup artifacts to an external storage(NFS) which can be mounted
 * to local system as normal storage device, and meantime keep to cache these artifacts to local storage as usual. And a cache
 * to store the usage ownership of the external storage will be hosted in this provider.
 */
@Named( "nfs-galley-cache" )
@Alternative
public class FastLocalCacheProvider
        implements CacheProvider, CacheProvider.AdminView
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public static final String NFS_BASE_DIR_KEY = "galley.nfs.basedir";

    private String nfsBaseDir;

    private final Map<ConcreteResource, Transfer> transferCache = new ConcurrentWeakKeyHashMap<>( 10000 );

    private final Map<String, Set<OutputStream>> streamHolder = new HashMap<>( 50 );

    @Inject
    @Named( "partyline-galley-cache" )
    private PartyLineCacheProvider plCacheProvider;

    // This NFS owner cache will be shared during nodes(indy?), it will record the which node is storing which file
    // in NFS. Used as <path, ip> cache to collect nfs ownership of the file storage
    @ConfigureCache( "nfs-owner-cache" )
    @NFSOwnerCache
    private Cache<String, String> nfsOwnerCache;

    private TransactionManager cacheTxMgr;

    @Inject
    private FileEventManager fileEventManager;

    @Inject
    private TransferDecorator transferDecorator;

    @ExecutorConfig( named = "fast-local-executor", threads = 5, priority = 2, daemon = true )
    @WeftManaged
    @Inject
    private Executor executor;

    protected FastLocalCacheProvider()
    {
        nfsBaseDir = System.getProperty( NFS_BASE_DIR_KEY );
    }

    public FastLocalCacheProvider( final PartyLineCacheProvider plCacheProvider,
                                   final Cache<String, String> nfsUsageCache, final FileEventManager fileEventManager,
                                   final TransferDecorator transferDecorator, final Executor executor )
    {
        this.plCacheProvider = plCacheProvider;
        this.nfsOwnerCache = nfsUsageCache;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.executor = executor;
    }

    public FastLocalCacheProvider( final PartyLineCacheProvider plCacheProvider,
                                   final Cache<String, String> nfsUsageCache, final FileEventManager fileEventManager,
                                   final TransferDecorator transferDecorator, final Executor executor,
                                   final String nfsBaseDir )
    {
        this( plCacheProvider, nfsUsageCache, fileEventManager, transferDecorator, executor );
        this.nfsBaseDir = nfsBaseDir;
    }

    @PostConstruct
    public void init()
    {
        cacheTxMgr = nfsOwnerCache.getAdvancedCache().getTransactionManager();

        startReporting();
    }

    @PreDestroy
    public void destroy()
    {
        stopReporting();
    }

    @Override
    public boolean isFileBased()
    {
        return true;
    }

    @Override
    public File getDetachedFile( ConcreteResource resource )
    {
        //FIXME: Is this Detached file local one or NFS one? Now it is local one first
        File file = plCacheProvider.getDetachedFile( resource );
        if ( file == null && StringUtils.isNotBlank( nfsBaseDir ) )
        {
            file = getNFSDetachedFile( resource );
        }

        return file;
    }

    private File getNFSDetachedFile( ConcreteResource resource )
    {
        String path = PathUtils.normalize( nfsBaseDir, getFilePath( resource ) );
        return new File( path );
    }

    @Override
    public void startReporting()
    {
        plCacheProvider.startReporting();
    }

    @Override
    public void stopReporting()
    {
        plCacheProvider.stopReporting();
    }

    @Override
    public void cleanupCurrentThread()
    {
        synchronized ( streamHolder )
        {
            plCacheProvider.cleanupCurrentThread();
            final String threadId = String.valueOf( Thread.currentThread().getId() );
            final Set<OutputStream> streams = streamHolder.get( threadId );
            if ( streams != null && !streams.isEmpty() )
            {
                for ( OutputStream stream : streams )
                {
                    try
                    {
                        stream.close();
                    }
                    catch ( IOException e )
                    {
                        final String errorMsg =
                                String.format( "[galley] I/O error happened when handling stream closing for stream %s",
                                               stream.toString() );
                        logger.error( errorMsg, e );
                    }
                }
            }
            streamHolder.remove( threadId );
        }

    }

    @Override
    public boolean isDirectory( ConcreteResource resource )
    {
        return getDetachedFile( resource ).isDirectory();
    }

    @Override
    public boolean isFile( ConcreteResource resource )
    {
        return getDetachedFile( resource ).isFile();
    }

    @Override
    public InputStream openInputStream( final ConcreteResource resource )
            throws IOException
    {
        boolean localExisted = plCacheProvider.exists( resource );
        if ( !localExisted )
        {
            final String pathKey = getKeyForResource( resource );
            executor.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        cacheTxMgr.begin();
                        nfsOwnerCache.getAdvancedCache().lock( pathKey );
                        File nfsFile = getNFSDetachedFile( resource );
                        final InputStream nfsStream = new FileInputStream( nfsFile );
                        final OutputStream localStream = plCacheProvider.openOutputStream( resource );
                        IOUtils.copy( nfsStream, localStream );
                    }
                    catch ( NotSupportedException | SystemException | IOException e )
                    {
                        final String errorMsg =
                                String.format( "[galley] Cache TransactionManager got error, locking key is %s",
                                               pathKey );
                        logger.error( errorMsg, e );
                        throw new IllegalStateException( errorMsg, e );
                    }
                    finally
                    {
                        try
                        {
                            cacheTxMgr.rollback();
                        }
                        catch ( SystemException e )
                        {
                            final String errorMsg = String.format(
                                    "[galley] Cache TransactionManager rollback got error, locking key is %s",
                                    pathKey );
                            logger.error( errorMsg, e );
                        }
                    }
                }
            } );

        }
        return plCacheProvider.openInputStream( resource );
    }

    @Override
    public OutputStream openOutputStream( ConcreteResource resource )
            throws IOException
    {
        OutputStream dualOut;
        try
        {
            cacheTxMgr.begin();
            final String nodeIp = getCurrentNodeIp();
            final String pathKey = getKeyForResource( resource );
            //FIXME: putIfAbsent?
            nfsOwnerCache.getAdvancedCache().lock( pathKey );
            nfsOwnerCache.put( pathKey, nodeIp );
            final OutputStream localOutputStream = plCacheProvider.openOutputStream( resource );
            File nfsFile = getNFSDetachedFile( resource );
            if ( !nfsFile.exists() && !nfsFile.isDirectory() )
            {
                try
                {
                    if ( !nfsFile.getParentFile().exists() )
                    {
                        nfsFile.getParentFile().mkdirs();
                    }
                    nfsFile.createNewFile();
                }
                catch ( IOException e )
                {
                    logger.error( "[galley] New nfs file created not properly.", e );
                }
            }
            final OutputStream nfsOutputStream = new FileOutputStream( nfsFile );
            dualOut = new DualOutputStreamsWrapper( localOutputStream, nfsOutputStream, cacheTxMgr );
            synchronized ( streamHolder )
            {
                final String threadId = String.valueOf( Thread.currentThread().getId() );
                Set<OutputStream> streams = streamHolder.get( threadId );
                if ( streams == null )
                {
                    streams = new HashSet<>( 10 );
                }
                streams.add( dualOut );
                streamHolder.put( threadId, streams );
            }
        }
        catch ( NotSupportedException | SystemException e )
        {
            logger.error( "[galley] Transaction error for nfs cache during file writing.", e );
            throw new IllegalStateException(
                    String.format( "[galley] Output stream for resource %s open failed.", resource.toString() ), e );
        }

        return dualOut;
    }

    @Override
    public boolean exists( ConcreteResource resource )
    {
        return plCacheProvider.exists( resource ) || getNFSDetachedFile( resource ).exists();
    }

    @Override
    public void copy( ConcreteResource from, ConcreteResource to )
            throws IOException
    {
        final String fromNFSPath = getKeyForResource( from );
        final String toNFSPath = getKeyForResource( to );
        try
        {
            cacheTxMgr.begin();
            nfsOwnerCache.getAdvancedCache().lock( fromNFSPath, toNFSPath );
            plCacheProvider.copy( from, to );
            final FileInputStream nfsFrom = new FileInputStream( getNFSDetachedFile( from ) );
            File nfsToFile = getNFSDetachedFile( to );
            if ( !nfsToFile.exists() && !nfsToFile.isDirectory() )
            {
                if ( !nfsToFile.getParentFile().exists() )
                {
                    nfsToFile.getParentFile().mkdirs();
                }
                try
                {
                    nfsToFile.createNewFile();
                }
                catch ( IOException e )
                {
                    logger.error( "[galley] New nfs file created not properly.", e );
                }
            }
            final OutputStream nfsTo = new FileOutputStream( nfsToFile );
            IOUtils.copy( nfsFrom, nfsTo );
            //FIXME: putIfAbsent? Or no need to put?
            nfsOwnerCache.put( toNFSPath, getCurrentNodeIp() );
            cacheTxMgr.commit();
        }
        catch ( NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException e )
        {
            logger.error( "[galley] Transaction error for nfs cache during file copying.", e );
            try
            {
                cacheTxMgr.rollback();
            }
            catch ( SystemException se )
            {
                final String errorMsg = "[galley] Transaction rollback error for nfs cache during file copying.";
                logger.error( errorMsg, se );
                throw new IllegalStateException( errorMsg, se );
            }
        }

    }

    @Override
    public String getFilePath( ConcreteResource resource )
    {
        try
        {
            return getDetachedFile( resource ).getCanonicalPath();
        }
        catch ( IOException e )
        {
            final String errorMsg = "[galley] File path not correctly got.";
            logger.error( errorMsg, e );
            throw new IllegalStateException( errorMsg, e );
        }
    }

    @Override
    public boolean delete( ConcreteResource resource )
            throws IOException
    {
        final File nfsFile = getNFSDetachedFile( resource );
        final String pathKey = getKeyForPath( nfsFile.getCanonicalPath() );
        try
        {
            cacheTxMgr.begin();
            nfsOwnerCache.getAdvancedCache().lock( pathKey );
            final boolean localDeleted = plCacheProvider.delete( resource );
            if ( !localDeleted )
            {
                logger.info( "local file deletion failed for {}", resource );
            }
            final boolean nfsDeleted = nfsFile.delete();
            if ( !nfsDeleted )
            {
                logger.info( "nfs file deletion failed for {}", nfsFile );
            }
            nfsOwnerCache.remove( pathKey );
            return localDeleted && nfsDeleted;
        }
        catch ( NotSupportedException | SystemException e )
        {
            final String errorMsg =
                    String.format( "[galley] Cache TransactionManager got error, locking key is %s", pathKey );
            logger.error( errorMsg, e );
            throw new IllegalStateException( errorMsg, e );
        }
        finally
        {
            try
            {
                cacheTxMgr.rollback();
            }
            catch ( SystemException e )
            {
                final String errorMsg =
                        String.format( "[galley] Cache TransactionManager rollback got error, locking key is %s",
                                       pathKey );
                logger.error( errorMsg, e );
            }
        }
    }

    @Override
    public String[] list( ConcreteResource resource )
    {
        // Only focus on NFS location
        return getNFSDetachedFile( resource ).list();
    }

    @Override
    public void mkdirs( ConcreteResource resource )
            throws IOException
    {
        final String pathKey = getKeyForResource( resource );
        try
        {
            cacheTxMgr.begin();
            nfsOwnerCache.getAdvancedCache().lock( pathKey );
            getDetachedFile( resource ).mkdirs();
        }
        catch ( NotSupportedException | SystemException e )
        {
            final String errorMsg =
                    String.format( "[galley] Cache TransactionManager got error, locking key is %s", pathKey );
            logger.error( errorMsg, e );
            throw new IllegalStateException( errorMsg, e );
        }
        finally
        {
            try
            {
                cacheTxMgr.rollback();
            }
            catch ( SystemException e )
            {
                final String errorMsg =
                        String.format( "[galley] Cache TransactionManager rollback got error, locking key is %s",
                                       pathKey );
                logger.error( errorMsg, e );
            }
        }
    }

    @Deprecated
    @Override
    public void createFile( ConcreteResource resource )
            throws IOException
    {
        final String pathKey = getKeyForResource( resource );
        try
        {
            cacheTxMgr.begin();
            nfsOwnerCache.getAdvancedCache().lock( pathKey );
            final File nfsFile = getNFSDetachedFile( resource );
            if ( !nfsFile.exists() )
            {
                nfsFile.getParentFile().mkdirs();
                nfsFile.createNewFile();
            }
        }
        catch ( NotSupportedException | SystemException e )
        {
            final String errorMsg =
                    String.format( "[galley] Cache TransactionManager got error, locking key is %s", pathKey );
            logger.error( errorMsg, e );
            throw new IllegalStateException( errorMsg, e );
        }
        finally
        {
            try
            {
                cacheTxMgr.rollback();
            }
            catch ( SystemException e )
            {
                final String errorMsg =
                        String.format( "[galley] Cache TransactionManager rollback got error, locking key is %s",
                                       pathKey );
                logger.error( errorMsg, e );
            }
        }
    }

    @Deprecated
    @Override
    public void createAlias( ConcreteResource from, ConcreteResource to )
            throws IOException
    {
        // if the download landed in a different repository, copy it to the current one for
        // completeness..., and both in local and nfs sides
        final Location fromKey = from.getLocation();
        final Location toKey = to.getLocation();
        final String fromPath = from.getPath();
        final String toPath = to.getPath();

        if ( fromKey != null && toKey != null && !fromKey.equals( toKey ) && fromPath != null && toPath != null
                && !fromPath.equals( toPath ) )
        {
            copy( from, to );
        }
    }

    @Override
    public synchronized Transfer getTransfer( ConcreteResource resource )
    {
        Transfer t = transferCache.get( resource );
        if ( t == null )
        {
            t = new Transfer( resource, this, fileEventManager, transferDecorator );
            transferCache.put( resource, t );
        }

        return t;
    }

    @Override
    public void clearTransferCache()
    {
        transferCache.clear();
    }

    @Override
    public long length( ConcreteResource resource )
    {
        return getDetachedFile( resource ).length();
    }

    @Override
    public long lastModified( ConcreteResource resource )
    {
        return getDetachedFile( resource ).lastModified();
    }

    @Override
    public boolean isReadLocked( ConcreteResource resource )
    {
        return nfsOwnerCache.getAdvancedCache().getLockManager().isLocked( getKeyForResource( resource ) );
    }

    @Override
    public boolean isWriteLocked( ConcreteResource resource )
    {
        // Use the same lock supplied by ISPN between write and read.
        return isReadLocked( resource );
    }

    @Override
    public void unlockRead( ConcreteResource resource )
    {

    }

    @Override
    public void unlockWrite( ConcreteResource resource )
    {

    }

    @Override
    public void lockRead( ConcreteResource resource )
    {

    }

    @Override
    public void lockWrite( ConcreteResource resource )
    {

    }

    @Override
    public void waitForReadUnlock( ConcreteResource resource )
    {
        while ( isReadLocked( resource ) )
        {
            logger.debug( "lock is still held for key {} by ISPN locker", getKeyForResource( resource ) );
            try
            {
                // Use ISPN lock owner for resource to wait until lock is released. Note that if the lock has no owner,
                // means lock has been released
                final Object owner =
                        nfsOwnerCache.getAdvancedCache().getLockManager().getOwner( getKeyForResource( resource ) );
                if ( owner == null )
                {
                    break;
                }
                owner.wait( 1000 );
            }
            catch ( final InterruptedException e )
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void waitForWriteUnlock( ConcreteResource resource )
    {
        // Use the same lock supplied by ISPN between write and read.
        waitForReadUnlock( resource );
    }

    private String getCurrentNodeIp()
            throws SocketException
    {

        final Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

        while ( nis.hasMoreElements() )
        {
            final NetworkInterface ni = nis.nextElement();
            final Enumeration<InetAddress> ips = ni.getInetAddresses();
            while ( ips.hasMoreElements() )
            {
                final InetAddress ip = ips.nextElement();
                if ( ip instanceof Inet4Address && ip.isSiteLocalAddress() )
                {
                    return ip.getHostAddress();
                }
            }
        }

        throw new IllegalStateException( "[galley] IP not found." );
    }

    private String getKeyForResource( ConcreteResource resource )
    {
        try
        {
            File nfsFile = getNFSDetachedFile( resource );
            // Need the key as a parent folder level to lock all files I/O with some derivative files like checksum files
            return getKeyForPath(
                    nfsFile.isDirectory() ? nfsFile.getCanonicalPath() : nfsFile.getParentFile().getCanonicalPath() );
        }
        catch ( IOException e )
        {
            final String errorMsg = String.format( "[galley] When get NFS cache key for resource: %s, got I/O error.",
                                                   resource.toString() );
            logger.error( errorMsg, e );
            throw new IllegalStateException( errorMsg, e );
        }
    }

    private String getKeyForPath( String path )
    {
        //TODO: will directly return path now, may change some other way future(like digesting?)
        return path;
    }
}
