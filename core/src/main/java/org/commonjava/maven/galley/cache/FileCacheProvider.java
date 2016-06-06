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
package org.commonjava.maven.galley.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.util.AtomicFileOutputStreamWrapper;
import org.commonjava.maven.galley.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named( "file-galley-cache" )
@Alternative
public class FileCacheProvider
    implements CacheProvider
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Map<ConcreteResource, Transfer> transferCache = new ConcurrentHashMap<ConcreteResource, Transfer>( 10000 );

    @Inject
    private FileCacheProviderConfig config;

    @Inject
    private PathGenerator pathGenerator;

    @Inject
    private FileEventManager fileEventManager;

    @Inject
    private TransferDecorator transferDecorator;

    private final SimpleLockingSupport lockingSupport = new SimpleLockingSupport();

    protected FileCacheProvider()
    {
    }

    public FileCacheProvider( final File cacheBasedir, final PathGenerator pathGenerator, final FileEventManager fileEventManager,
                              final TransferDecorator transferDecorator, final boolean aliasLinking )
    {
        this.pathGenerator = pathGenerator;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.config = new FileCacheProviderConfig( cacheBasedir ).withAliasLinking( aliasLinking );
    }

    public FileCacheProvider( final FileCacheProviderConfig config, final PathGenerator pathGenerator, final FileEventManager fileEventManager,
                              final TransferDecorator transferDecorator )
    {
        this.config = config;
        this.pathGenerator = pathGenerator;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
    }

    public FileCacheProvider( final File cacheBasedir, final PathGenerator pathGenerator, final FileEventManager fileEventManager,
                              final TransferDecorator transferDecorator )
    {
        this( cacheBasedir, pathGenerator, fileEventManager, transferDecorator, true );
    }

    @Override
    public File getDetachedFile( final ConcreteResource resource )
    {
        // TODO: this might be a bit heavy-handed, but we need to be sure. 
        // Maybe I can improve it later.
        final Transfer txfr = getTransfer( resource );
        synchronized ( txfr )
        {
            File f = getRawFile( resource );
            if ( resource.isRoot() && !f.isDirectory() )
            {
                f.mkdirs();
            }

            // TODO: configurable default timeout
            final int timeoutSeconds = resource.getLocation()
                        .getAttribute( Location.CACHE_TIMEOUT_SECONDS, Integer.class,
                                       Location.DEFAULT_CACHE_TIMEOUT_SECONDS );

            if ( !resource.isRoot() && f.exists() && !f.isDirectory() && timeoutSeconds > 0 )
            {
                final long current = System.currentTimeMillis();
                final long lastModified = f.lastModified();
                final int tos =
                    timeoutSeconds < Location.MIN_CACHE_TIMEOUT_SECONDS ? Location.MIN_CACHE_TIMEOUT_SECONDS
                                    : timeoutSeconds;

                final long timeout = TimeUnit.MILLISECONDS.convert( tos, TimeUnit.SECONDS );

                if ( current - lastModified > timeout )
                {
                    final File mved = new File( f.getPath() + SUFFIX_TO_DELETE );
                    f.renameTo( mved );

                    try
                    {
                        logger.info( "Deleting cached file: {} (moved to: {})\n  due to timeout after: {}\n  elapsed: {}\n  original timeout in seconds: {}",
                                     f, mved, timeout, ( current - lastModified ), tos );

                        if ( mved.exists() )
                        {
                            FileUtils.forceDelete( mved );
                        }
                    }
                    catch ( final IOException e )
                    {
                        logger.error( String.format( "Failed to delete: %s.", f ), e );
                    }
                }
            }

            return f;
        }
    }

    private File getRawFile( ConcreteResource resource )
    {
        final String altDir = resource.getLocation()
                                      .getAttribute( Location.ATTR_ALT_STORAGE_LOCATION, String.class );

        File f = null;
        if ( altDir == null )
        {
            f = new File( getFilePath( resource ) );
        }
        else
        {
            f = new File( altDir, resource.getPath() );
        }

        return f;
    }

    @Override
    public boolean isDirectory( final ConcreteResource resource )
    {
        final File f = getDetachedFile( resource );
        return f.isDirectory();
    }

    @Override
    public boolean isFile( final ConcreteResource resource )
    {
        final File f = getDetachedFile( resource );
        return f.isFile();
    }

    @Override
    public InputStream openInputStream( final ConcreteResource resource )
        throws IOException
    {
        return new FileInputStream( getDetachedFile( resource ) );
    }

    @Override
    public OutputStream openOutputStream( final ConcreteResource resource )
        throws IOException
    {
        final File targetFile = getDetachedFile( resource );

        final File dir = targetFile.getParentFile();
        if ( !dir.isDirectory() && !dir.mkdirs() )
        {
            throw new IOException( "Cannot create directory: " + dir );
        }

        final File downloadFile = new File( targetFile.getPath() + CacheProvider.SUFFIX_TO_WRITE );
        final FileOutputStream stream = new FileOutputStream( downloadFile );

        return new AtomicFileOutputStreamWrapper( targetFile, downloadFile, stream );
    }

    @Override
    public boolean exists( final ConcreteResource resource )
    {
        final File f = getRawFile( resource );
        //        logger.info( "Checking for existence of cache file: {}", f );
        return f.exists();
    }

    @Override
    public void copy( final ConcreteResource from, final ConcreteResource to )
        throws IOException
    {
        FileUtils.copyFile( getDetachedFile( from ), getDetachedFile( to ) );
    }

    @Override
    public boolean delete( final ConcreteResource resource )
        throws IOException
    {
        return getDetachedFile( resource ).delete();
    }

    @Override
    public String[] list( final ConcreteResource resource )
    {
        final String[] listing = getDetachedFile( resource ).list();
        if ( listing == null )
        {
            return null;
        }

        final List<String> list = new ArrayList<String>( Arrays.asList( listing ) );
        for ( final Iterator<String> it = list.iterator(); it.hasNext(); )
        {
            final String fname = it.next();
            if ( fname.charAt( 0 ) == '.' )
            {
                it.remove();
                continue;
            }

            for ( final String suffix : HIDDEN_SUFFIXES )
            {
                if ( fname.endsWith( suffix ) )
                {
                    it.remove();
                }
            }
        }

        return list.toArray( new String[list.size()] );
    }

    @Override
    public void mkdirs( final ConcreteResource resource )
        throws IOException
    {
        getDetachedFile( resource ).mkdirs();
    }

    @Override
    public void createFile( final ConcreteResource resource )
        throws IOException
    {
        getDetachedFile( resource ).createNewFile();
    }

    @Override
    public void createAlias( final ConcreteResource from, final ConcreteResource to )
        throws IOException
    {
        // if the download landed in a different repository, copy it to the current one for
        // completeness...
        final Location fromKey = from.getLocation();
        final Location toKey = to.getLocation();
        final String fromPath = from.getPath();
        final String toPath = to.getPath();

        if ( fromKey != null && toKey != null && !fromKey.equals( toKey ) && fromPath != null && toPath != null && !fromPath.equals( toPath ) )
        {
            if ( config.isAliasLinking() )
            {
                final File fromFile = getDetachedFile( from );
                final File toFile = getDetachedFile( to );

                FileUtils.copyFile( fromFile, toFile );
                //                Files.createLink( Paths.get( fromFile.toURI() ), Paths.get( toFile.toURI() ) );
            }
            else
            {
                copy( from, to );
            }
        }
    }

    @Override
    public String getFilePath( final ConcreteResource resource )
    {
        String dir = resource.getLocation()
                             .getAttribute( Location.ATTR_ALT_STORAGE_LOCATION, String.class );

        if ( dir == null )
        {
            dir = config.getCacheBasedir().getPath();
        }

        return PathUtils.normalize( dir, pathGenerator.getFilePath( resource ) );
    }

    @Override
    public synchronized Transfer getTransfer( final ConcreteResource resource )
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
    public long length( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).length();
    }

    @Override
    public long lastModified( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).lastModified();
    }

    @Override
    public boolean isReadLocked( final ConcreteResource resource )
    {
        return lockingSupport.isLocked( resource );
    }

    @Override
    public boolean isWriteLocked( final ConcreteResource resource )
    {
        return lockingSupport.isLocked( resource );
    }

    @Override
    public void unlockRead( final ConcreteResource resource )
    {
        lockingSupport.unlock( resource );
    }

    @Override
    public void unlockWrite( final ConcreteResource resource )
    {
        lockingSupport.unlock( resource );
    }

    @Override
    public void lockRead( final ConcreteResource resource )
    {
        lockingSupport.lock( resource );
    }

    @Override
    public void lockWrite( final ConcreteResource resource )
    {
        lockingSupport.lock( resource );
    }

    @Override
    public void waitForWriteUnlock( final ConcreteResource resource )
    {
        lockingSupport.waitForUnlock( resource );
    }

    @Override
    public void waitForReadUnlock( final ConcreteResource resource )
    {
        lockingSupport.waitForUnlock( resource );
    }

    @Override
    public void cleanupCurrentThread()
    {
        lockingSupport.cleanupCurrentThread();
    }

    @Override
    public void startReporting()
    {
        lockingSupport.startReporting();
    }

    @Override
    public void stopReporting()
    {
        lockingSupport.stopReporting();
    }
}
