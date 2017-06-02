/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.cache.infinispan;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.commonjava.cdi.util.weft.ThreadContext;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.util.PathUtils;
import org.infinispan.commons.util.concurrent.ConcurrentWeakKeyHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This cache provider provides the ability to write the backup artifacts to an external storage(NFS) which can be mounted
 * to local system as normal storage device, and meantime keep to cache these artifacts to local storage as usual. And a cache
 * to store the usage ownership of the external storage will be hosted in this provider. <br />
 * As this cache provider will use NFS as the distributed file caching, the NFS root directory is needed. If you want use this cache
 * provider in CDI environment, please don't forget to set the system property "galley.nfs.basedir" to specify this directory
 * as this provider will use it to get the nfs root directory by default. If you want to set this directory by manually,
 * use the parameterized constructor with the "nfsBaseDir" param.
 */
public class FastLocalCacheProvider
        implements CacheProvider, CacheProvider.AdminView
{

    private static final String FAST_LOCAL_STREAMS = "fast-local-streams";

    private static final String CURRENT_THREAD_STATE = "current-thread-state";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public static final String NFS_BASE_DIR_KEY = "galley.nfs.basedir";

    private String nfsBaseDir;

    // use weak key map to avoid the memory occupy for long time of the transfer
    private final Map<ConcreteResource, Transfer> transferCache = new ConcurrentWeakKeyHashMap<>( 10000 );

    private PartyLineCacheProvider plCacheProvider;

    // This NFS owner cache will be shared during nodes(indy?), it will record the which node is storing which file
    // in NFS. Used as <path, ip> cache to collect nfs ownership of the file storage
    private final CacheInstance<String, String> nfsOwnerCache;

    private FileEventManager fileEventManager;

    private TransferDecorator transferDecorator;

    private ExecutorService executor;

    private PathGenerator pathGenerator;



    /**
     * Construct the FastLocalCacheProvider with the params. You can specify you own nfs base dir in this constructor.
     *
     * @param plCacheProvider - PartyLineCacheProvider to handle the local cache files
     * @param nfsUsageCache - ISPN cache to hold the nfs artifacts owner.
     * @param fileEventManager -
     * @param transferDecorator -
     * @param executor - The thread pool for executing reading task concurrently.
     * @param nfsBaseDir - The NFS system root dir to hold the artifacts
     */
    protected FastLocalCacheProvider( final PartyLineCacheProvider plCacheProvider,
                                      final CacheInstance<String, String> nfsUsageCache, final PathGenerator pathGenerator,
                                      final FileEventManager fileEventManager, final TransferDecorator transferDecorator,
                                      final ExecutorService executor, final String nfsBaseDir )
    {
        this.plCacheProvider = plCacheProvider;
        this.nfsOwnerCache = nfsUsageCache;
        this.pathGenerator = pathGenerator;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.executor = executor;
        setNfsBaseDir( nfsBaseDir );
        init();
    }

    private void checkNfsBaseDir(){
        if ( StringUtils.isEmpty( nfsBaseDir ) )
        {
            logger.debug( ">>>[galley] the nfs basedir is {}", nfsBaseDir );
            throw new IllegalArgumentException(
                    "[galley] FastLocalCacheProvider needs nfs directory to cache files, please set the parameter correctly or use system property \"galley.nfs.basedir\" first with your NFS root directory." );
        }
    }

    /**
     * Sets the nfs base dir. Note that if the nfsBaseDir is not valid(empty or not a directory), then will check the system property
     * "galley.nfs.basedir" to get the value again. If still not valid, will throw Exception
     *
     * @param nfsBaseDir -
     * @throws java.lang.IllegalArgumentException - the nfsBaseDir is not valid(empty or not a valid directory)
     */
    public void setNfsBaseDir( String nfsBaseDir )
    {
        this.nfsBaseDir = nfsBaseDir;
        if ( StringUtils.isBlank( this.nfsBaseDir )  )
        {
            logger.warn( "[galley] nfs basedir {} is not valid directory", this.nfsBaseDir );
            this.nfsBaseDir = System.getProperty( NFS_BASE_DIR_KEY );
        }
        checkNfsBaseDir();
    }

    @PostConstruct
    public void init()
    {
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

    File getNFSDetachedFile( ConcreteResource resource )
    {
        File f = new File( getNFSFilePath( resource ) );
        synchronized ( this )
        {
            if ( resource.isRoot() && !f.isDirectory() )
            {
                f.mkdirs();
            }
        }
        return f;
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
        plCacheProvider.cleanupCurrentThread();

        ThreadContext streamHolder = ThreadContext.getContext( false );
        if ( streamHolder != null )
        {
            final String threadId = String.valueOf( Thread.currentThread().getId() );
            final Set<WeakReference<OutputStream>> streams = (Set<WeakReference<OutputStream>>) streamHolder.get( FAST_LOCAL_STREAMS );
            if ( streams != null && !streams.isEmpty() )
            {
                Iterator<WeakReference<OutputStream>> iter = streams.iterator();
                while ( iter.hasNext() )
                {
                    WeakReference<OutputStream> streamRef = iter.next();
                    IOUtils.closeQuietly( streamRef.get() );

                    iter.remove();
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

    /**
     * For file reading, first will check if the local cache has the file there. If yes, will directly to read the local
     * cache. If no, then will check the NFS volume for the file, and will copy it to the local cache if found, then read
     * from the local cache again.
     *
     * @param resource - the resource will be read
     * @return - the input stream for further reading
     * @throws IOException
     */
    @Override
    public InputStream openInputStream( final ConcreteResource resource )
            throws IOException
    {
        final String pathKey = getKeyForResource( resource );

        // This lock is used to control the the local resource can be opened successfully finally when local resource missing
        // but NFS not, which means will do a NFS->local copy.
        final Object copyLock = new Object();
        
        // A flag to mark if the local resource can be open now or need to wait for the copy thread completes its work
        final AtomicBoolean canStreamOpen = new AtomicBoolean( false );
        // This copy task is responsible for the NFS->local copy, and will be run in another thread,
        // which can use PartyLine concurrent read/write function on the local cache to boost
        // the i/o operation
        final Runnable copyNFSTask = () ->
        {
            InputStream nfsIn = null;
            OutputStream localOut = null;

            try
            {
                lockByISPN( nfsOwnerCache, resource );

                File nfsFile = getNFSDetachedFile( resource );
                if ( !nfsFile.exists() )
                {
                    logger.debug( "NFS cache does not exist too." );
                    return;
                }
                nfsIn = new FileInputStream( nfsFile );
                localOut = plCacheProvider.openOutputStream( resource );
                IOUtils.copy( nfsIn, localOut );
                logger.debug( "NFS copy to local cache done." );
            }
            catch ( NotSupportedException | SystemException | IOException e )
            {
                if ( e instanceof IOException )
                {
                    final String errorMsg =
                            String.format( "[galley] got i/o error when doing the NFS->Local copy for resource %s",
                                           resource.toString() );
                    logger.warn( errorMsg, e );
                }
                else
                {
                    final String errorMsg = String.format(
                            "[galley] Cache TransactionManager got error, locking key is %s, resource is %s", pathKey,
                            resource.toString() );
                    logger.error( errorMsg, e );
                    throw new IllegalStateException( errorMsg, e );
                }
            }
            finally
            {
                unlockByISPN( nfsOwnerCache, false );

                IOUtils.closeQuietly( nfsIn );
                IOUtils.closeQuietly( localOut );
                synchronized ( copyLock )
                {
                    canStreamOpen.set( true );
                    copyLock.notifyAll();
                }
            }
        };
        // This lock is used to control the concurrent operations on the resource, like concurrent delete and read/write.
        // Use "this" as lock is heavy, should think about use the transfer for the resource as the lock for each thread
        synchronized ( getTransfer( resource ) )
        {
            boolean localExisted = plCacheProvider.exists( resource );

            if ( localExisted )
            {
                logger.debug( "local cache already exists, will directly get input stream from it." );
                return plCacheProvider.openInputStream( resource );
            }
            else
            {
                logger.debug( "local cache does not exist, will start to copy from NFS cache" );
                executor.execute( copyNFSTask );
            }
            synchronized ( copyLock )
            {
                while ( !canStreamOpen.get() )
                {
                    try
                    {
                        copyLock.wait();
                    }
                    catch ( InterruptedException e )
                    {
                        logger.warn( "[galley] NFS copy thread is interrupted by other threads", e );
                    }
                }
                logger.debug("the NFS->local copy completed, will get the input stream from local cache");
                return plCacheProvider.openInputStream( resource );
            }
        }

    }

    /**
     * For file writing, will wrapping two output streams to caller - one for local cache file, another for nfs file -,
     * and the caller can write to these two streams in the meantime. <br />
     * For the local part, because it uses {@link org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider} as
     * i/o provider, this supports the R/W on the same resource in the meantime. For details, please see
     * {@link org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider}.
     *
     * @param resource - the resource will be read
     * @return - the output stream for further writing
     * @throws IOException
     */
    @Override
    public OutputStream openOutputStream( ConcreteResource resource )
            throws IOException
    {
        final OutputStream dualOut;
        final String nodeIp = getCurrentNodeIp();
        final String pathKey = getKeyForResource( resource );
        final File nfsFile = getNFSDetachedFile( resource );
        synchronized ( getTransfer( resource ) )
        {
            try
            {
                lockByISPN( nfsOwnerCache, resource );

                nfsOwnerCache.put( pathKey, nodeIp );

                logger.debug( "Start to get output stream from local cache through partyline to do join stream" );
                final OutputStream localOut = plCacheProvider.openOutputStream( resource );
                logger.debug( "The output stream from local cache through partyline is got successfully" );
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
                        throw e;
                    }
                }
                final OutputStream nfsOutputStream = new FileOutputStream( nfsFile );
                logger.debug( "The output stream from NFS is got successfully" );
                // will wrap the cache manager in stream wrapper, and let it do tx commit in stream close to make sure
                // the two streams writing's consistency.
                dualOut = new DualOutputStreamsWrapper( localOut, nfsOutputStream, nfsOwnerCache, pathKey, resource );

                if ( nfsOwnerCache.getLockOwner( pathKey ) != null )
                {
                    logger.trace( "ISPN locker for key {} with resource {} is {}",
                                  pathKey, resource, nfsOwnerCache.getLockOwner( pathKey ) );
                }

                ThreadContext streamHolder = ThreadContext.getContext( true );
                Set<WeakReference<OutputStream>> streams =
                        (Set<WeakReference<OutputStream>>) streamHolder.get( FAST_LOCAL_STREAMS );

                if ( streams == null )
                {
                    streams = new HashSet<>( 10 );
                }

                streams.add( new WeakReference<>( dualOut ) );
                streamHolder.put( FAST_LOCAL_STREAMS, streams );
            }
            catch ( NotSupportedException | SystemException e )
            {
                logger.error( "[galley] Transaction error for nfs cache during file writing.", e );
                throw new IllegalStateException(
                        String.format( "[galley] Output stream for resource %s open failed.", resource.toString() ),
                        e );
            }
            logger.debug( "The dual output stream wrapped and returned successfully" );
            return dualOut;
        }
    }

    private void lockByISPN(final CacheInstance<String, String> cacheInstance, final ConcreteResource resource )
            throws SystemException, NotSupportedException, IOException
    {
        //FIXME: This whole method is not thread-safe, especially for the lock state of the path, so the caller needs to take care

        // We need to think about the way of the ISPN lock and wait. If directly
        // use the nfsOwnerCache.lock but not consider if the lock has been acquired by another
        // thread, the ISPN lock will fail with a RuntimeException. So we need to let the
        // thread wait for the ISPN lock until it's released by the thread holds it. It's
        // like "tryLock" and "wait" of a thread lock.

        CacheInstance<String, String> cacheInst = cacheInstance;
        if ( cacheInst == null )
        {
            cacheInst = nfsOwnerCache;
        }

        final String path = getKeyForResource( resource );

        increaseThreadCount();

        // Some consideration about the thread "re-entrant" for waiting here. If it is the same
        // thread, will not wait.
        waitForISPNLock( resource, cacheInst.isLocked( path ) );

        if ( cacheInst.getTransactionStatus() == Status.STATUS_NO_TRANSACTION )
        {
            cacheInst.beginTransaction();
        }

        if ( !cacheInst.isLocked( path ) )
        {
            cacheInst.lock( path );
        }
    }

    private void unlockByISPN(final CacheInstance<String, String> cacheInstance, final boolean needCommit )
    {
        CacheInstance<String, String> cacheInst = cacheInstance;
        if ( cacheInst == null )
        {
            cacheInst = nfsOwnerCache;
        }
        final int threadState = decreaseThreadCount();
        if ( threadState == 0 )
        {
            if ( needCommit )
            {
                try
                {
                    if ( cacheInst.getTransactionStatus() == Status.STATUS_NO_TRANSACTION )
                    {
                        throw new IllegalStateException(
                                "[galley] Transaction has been completed before, no transaction associated by streams IO errors." );
                    }
                    logger.trace( "Transaction ended." );
                    cacheInst.commit();
                    return;
                }
                catch ( SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException e )
                {
                    logger.error( "[galley] Transaction commit error for nfs cache during file writing.", e );
                }
            }
            try
            {
                cacheInst.rollback();
            }
            catch ( SystemException se )
            {
                final String errorMsg = "[galley] Transaction rollback error for nfs cache during file writing.";
                logger.error( errorMsg, se );
                throw new IllegalStateException( errorMsg, se );
            }
        }
    }

    private int increaseThreadCount()
    {
        // This thread holder is used to add some "re-entrant" like function for the ISPN transaction lock. The ISPN lock is
        // used for ISPN transaction but not for thread level with re-entrant, that means if we want to own this lock in one
        // transaction for more than twice in single thread, it will block this thread. So we need this thread holder to by-pass
        // the ISPN lock waiting when thread not changed.
        final ThreadContext threadHolder = ThreadContext.getContext( true );
        AtomicInteger threadStateCount = (AtomicInteger) threadHolder.get( CURRENT_THREAD_STATE );
        if ( threadStateCount == null )
        {
            threadStateCount = new AtomicInteger( 0 );
        }
        final int threadState = threadStateCount.incrementAndGet();
        logger.trace( "thread state increased, current is {}", threadState );
        threadHolder.putIfAbsent( CURRENT_THREAD_STATE, threadStateCount );
        return threadState;
    }

    private int decreaseThreadCount()
    {
        final ThreadContext threadHolder = ThreadContext.getContext( false );
        if ( threadHolder == null )
        {
            return 0;
        }
        AtomicInteger threadStateCount = (AtomicInteger) threadHolder.get( CURRENT_THREAD_STATE );
        if ( threadStateCount != null )
        {
            final int threadState = threadStateCount.decrementAndGet();
            logger.trace( "thread state decreased, current is {}", threadState );
            return threadState;
        }
        else
        {
            return 0;
        }
    }

    private boolean isCurrentThread()
    {
        final ThreadContext threadHolder = ThreadContext.getContext( false );
        if ( threadHolder == null )
        {
            return false;
        }
        AtomicInteger threadCount = (AtomicInteger) threadHolder.get( CURRENT_THREAD_STATE );
        return threadCount != null && threadCount.get() > 0;
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
        //FIXME: there is no good solution here for thread locking as there are two resource needs to be locked. If handled not correctly, will cause dead lock
        InputStream nfsFrom = null;
        OutputStream nfsTo = null;
        try
        {
            //FIXME: need to think about this lock of the re-entrant way and ISPN lock wait
            nfsOwnerCache.beginTransaction();
            nfsOwnerCache.lock( fromNFSPath, toNFSPath );
            plCacheProvider.copy( from, to );
            nfsFrom = new FileInputStream( getNFSDetachedFile( from ) );
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
            nfsTo = new FileOutputStream( nfsToFile );
            IOUtils.copy( nfsFrom, nfsTo );
            //FIXME: need to use put?
            nfsOwnerCache.putIfAbsent( toNFSPath, getCurrentNodeIp() );
            nfsOwnerCache.commit();
        }
        catch ( NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException e )
        {
            logger.error( "[galley] Transaction error for nfs cache during file copying.", e );
            try
            {
                nfsOwnerCache.rollback();
            }
            catch ( SystemException se )
            {
                final String errorMsg = "[galley] Transaction rollback error for nfs cache during file copying.";
                logger.error( errorMsg, se );
                throw new IllegalStateException( errorMsg, se );
            }
        }
        finally
        {
            IOUtils.closeQuietly( nfsFrom );
            IOUtils.closeQuietly( nfsTo );
        }

    }

    @Override
    public String getFilePath( final ConcreteResource resource )
    {
        String dir = resource.getLocation()
                             .getAttribute( Location.ATTR_ALT_STORAGE_LOCATION, String.class );

        return dir != null ?
                PathUtils.normalize( dir, pathGenerator.getFilePath( resource ) ) :
                getNFSFilePath( resource );
    }

    private String getNFSFilePath(final ConcreteResource resource){
        return PathUtils.normalize( nfsBaseDir, pathGenerator.getFilePath( resource ) );
    }

    @Override
    public boolean delete( ConcreteResource resource )
            throws IOException
    {
        final File nfsFile = getNFSDetachedFile( resource );
        final String pathKey = getKeyForPath( nfsFile.getCanonicalPath() );
        synchronized ( getTransfer( resource ) )
        {
            boolean localDeleted = false;
            try
            {
                // must make sure the local file is not in reading/writing status
                if ( !plCacheProvider.isWriteLocked( resource ) && !plCacheProvider.isReadLocked( resource ) )
                {
                    logger.debug( "[galley] Local cache file is not locked, will be deleted now." );
                    localDeleted = plCacheProvider.delete( resource );
                }
                else
                {
                    logger.warn(
                            "Resource {} is locked by other threads for waiting and writing, can not be deleted now",
                            resource );
                }
                if ( !localDeleted )
                {
                    // if local deletion not success, no need to delete NFS to keep data consistency
                    logger.info( "local file deletion failed for {}", resource );
                    return false;
                }
                lockByISPN( nfsOwnerCache, resource );
                nfsOwnerCache.remove( pathKey );
                final boolean nfsDeleted = nfsFile.delete();
                if ( !nfsDeleted )
                {
                    logger.info( "nfs file deletion failed for {}", nfsFile );
                }
                return nfsDeleted;
            }
            catch ( NotSupportedException | SystemException e )
            {
                final String errorMsg = String.format( "[galley] Cache TransactionManager got error, locking key is %s", pathKey );
                logger.error( errorMsg, e );
                throw new IllegalStateException( errorMsg, e );
            }
            finally
            {
                if ( localDeleted )
                {
                    unlockByISPN( nfsOwnerCache, false );
                }
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
            lockByISPN( nfsOwnerCache, resource );
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
            unlockByISPN( nfsOwnerCache, false );
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
            lockByISPN( nfsOwnerCache, resource );
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
            unlockByISPN( nfsOwnerCache, false );
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
        return transferCache.computeIfAbsent( resource,
                                                    r -> new Transfer( r, this, fileEventManager, transferDecorator ) );
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
        try
        {
            //FIXME: potential dead lock?
            synchronized ( getTransfer( resource ) )
            {
                return plCacheProvider.isReadLocked( resource ) || nfsOwnerCache.isLocked(
                        getKeyForResource( resource ) );
            }
        }
        catch ( IOException e )
        {
            final String errorMsg = String.format( "[galley] When get NFS cache key for resource: %s, got I/O error.",
                                                   resource.toString() );
            logger.error( errorMsg, e );
            throw new IllegalStateException( errorMsg, e );
        }
    }

    @Override
    public boolean isWriteLocked( ConcreteResource resource )
    {
        try
        {
            //FIXME: potential dead lock?
            synchronized ( getTransfer( resource ) )
            {
                return plCacheProvider.isWriteLocked( resource ) || nfsOwnerCache.isLocked(
                        getKeyForResource( resource ) );
            }
        }
        catch ( IOException e )
        {
            final String errorMsg = String.format( "[galley] When get NFS cache key for resource: %s, got I/O error.",
                                                   resource.toString() );
            logger.error( errorMsg, e );
            throw new IllegalStateException( errorMsg, e );
        }
    }

    @Override
    public void unlockRead( ConcreteResource resource )
    {
        // Not supported yet
    }

    @Override
    public void unlockWrite( ConcreteResource resource )
    {
        // Not supported yet
    }

    @Override
    public void lockRead( ConcreteResource resource )
    {
        // Not supported yet
    }

    @Override
    public void lockWrite( ConcreteResource resource )
    {
        // Not supported yet
    }

    @Override
    public void waitForReadUnlock( ConcreteResource resource )
    {
        //FIXME: potential dead lock?
        synchronized ( getTransfer( resource ) )
        {
            plCacheProvider.waitForReadUnlock( resource );
            waitForISPNLock( resource, isReadLocked( resource ) );
        }

    }

    @Override
    public AdminView asAdminView()
    {
        return this;
    }

    @Override
    public void waitForWriteUnlock( ConcreteResource resource )
    {
        //FIXME: potential dead lock?
        synchronized ( getTransfer( resource ) )
        {
            plCacheProvider.waitForWriteUnlock( resource );
            waitForISPNLock( resource, isWriteLocked( resource ) );
        }
    }

    private void waitForISPNLock( ConcreteResource resource, boolean locked )
    {

        if ( isCurrentThread() )
        {
            logger.trace( "Processing in same thread, will not wait for ISPN lock to make it re-entrant" );
            return;
        }

        final String key;
        try
        {
            key = getKeyForResource( resource );
        }
        catch ( IOException e )
        {
            final String errorMsg =
                    String.format( "[galley] When get NFS cache key for resource: %s, got I/O error.", resource.toString() );
            logger.error( errorMsg, e );
            throw new IllegalStateException( errorMsg, e );
        }

        while ( locked )
        {
            try
            {
                // Use ISPN lock owner for resource to wait until lock is released. Note that if the lock has no owner,
                // means lock has been released
                final Object owner = nfsOwnerCache.getLockOwner( key );
                if ( owner == null )
                {
                    break;
                }

                synchronized ( owner )
                {
                    logger.trace(
                            "ISPN lock still not released. ISPN lock key:{}, locker: {}, operation path: {}. Waiting for 0.1s",
                            key, owner, resource );
                    owner.wait( 1000 );
                }
            }
            catch ( final InterruptedException e )
            {
                Thread.currentThread().interrupt();
            }
        }
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

    private String getKeyForResource( ConcreteResource resource ) throws IOException
    {
        File nfsFile = getNFSDetachedFile( resource );
        // Need the key as a parent folder level to lock all files I/O with some derivative files like checksum files
        return getKeyForPath(
                nfsFile.isDirectory() ? nfsFile.getCanonicalPath() : nfsFile.getParentFile().getCanonicalPath() );

    }

    private String getKeyForPath( String path )
    {
        //TODO: will directly return path now, may change some other way future(like digesting?)
        return path;
    }


    /**
     * A output stream wrapper to let the stream writing to dual output stream
     */
    private final class DualOutputStreamsWrapper
            extends OutputStream
    {

        private final OutputStream out1;

        private final OutputStream out2;

        private final CacheInstance<String,String> cacheInstance;

        private boolean closed = false;

        private final String cacheKey;

        private final ConcreteResource resource;

        public DualOutputStreamsWrapper( final OutputStream out1, final OutputStream out2,
                                         final CacheInstance<String, String> cacheInstance, final String cacheKey, final ConcreteResource resource )
        {
            if ( cacheInstance == null )
            {
                throw new NullPointerException( "Cache instance cannot be null." );
            }

            if ( out1 == null || out2 == null )
            {
                throw new NullPointerException( "Output streams cannot be null: (stream1: " + out1 + " / stream2: " + out2 + ")" );
            }

            this.out1 = out1;
            this.out2 = out2;
            this.cacheInstance = cacheInstance;
            this.cacheKey = cacheKey;
            this.resource = resource;
        }

        @Override
        public void write( int b )
                throws IOException
        {
            out1.write( b );
            out2.write( b );
        }

        public void write( byte b[] )
                throws IOException
        {
            write( b, 0, b.length );
        }

        @Override
        public void write( byte b[], int off, int len )
                throws IOException
        {
            out1.write( b, off, len );
            out2.write( b, off, len );
        }

        public void flush()
                throws IOException
        {
            out1.flush();
            out2.flush();
        }

        public void close()
                throws IOException
        {
            // To resolve "Double-close" issue, add this closed flag for the "real closed" recognition
            final Logger logger = FastLocalCacheProvider.this.logger;
            if ( closed )
            {
                logger.trace( "The DualOutputStream {} already closed.", this );
                // If still ISPN locked, we should unlock it to avoid "lock-never-released"
                if ( cacheInstance.isLocked( cacheKey ) )
                {
                    unlockByISPN( cacheInstance, false );
                }
                return;
            }

            logger.trace( "ISPN lock released before ISPN trasaction for key {} with resource {}? {}", cacheKey,
                          resource, cacheInstance.getLockOwner( cacheKey ) == null ? "Yes" : "No" );
            if ( cacheInstance.getLockOwner( cacheKey ) != null )
            {
                logger.trace( "ISPN locker for key {} with resource {} is {}",
                              cacheKey, resource, cacheInstance.getLockOwner( cacheKey ) );
            }

            try
            {
                unlockByISPN( cacheInstance, true );

                // To avoid ISPN lock not released correctly, should consider the real closed case after the lock released successfully
                if ( !closed )
                {
                    closed = true;
                }
            }
            finally
            {
                // For safe, we should always let the stream closed, even if the transaction failed.
                IOUtils.closeQuietly( out1 );
                IOUtils.closeQuietly( out2 );

                logger.trace( "ISPN lock released after ISPN trasaction for key {} with resource {}? {}", cacheKey, resource,
                              cacheInstance.getLockOwner( cacheKey ) == null ? "Yes" : "No" );
            }
        }
    }

}
