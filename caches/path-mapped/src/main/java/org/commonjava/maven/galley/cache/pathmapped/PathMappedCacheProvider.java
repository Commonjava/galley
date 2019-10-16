/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.cache.pathmapped;

import org.commonjava.cdi.util.weft.SingleThreadedExecutorService;
import org.commonjava.maven.galley.cache.pathmapped.core.PathMappedFileManager;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PathMappedCacheProvider
                implements CacheProvider, CacheProvider.AdminView
{
    private static final long SWEEP_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private PathMappedCacheProviderConfig config;

    private FileEventManager fileEventManager;

    private TransferDecoratorManager transferDecorator;

    private PathMappedFileManager fileManager;

    private ScheduledExecutorService deleteExecutor;

    private List<Transfer> toDelete = Collections.synchronizedList( new ArrayList<>() );

    public PathMappedCacheProvider( final File cacheBasedir,
                                    final FileEventManager fileEventManager,
                                    final TransferDecoratorManager transferDecorator,
                                    final ScheduledExecutorService deleteExecutor,
                                    final PathMappedFileManager fileManager )
    {
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.config = new PathMappedCacheProviderConfig( cacheBasedir );

        int corePoolSize = 2;
        this.deleteExecutor =
                        deleteExecutor == null ? Executors.newScheduledThreadPool( corePoolSize ) : deleteExecutor;
        this.fileManager = fileManager;

        if ( deleteExecutor instanceof ThreadPoolExecutor )
        {
            corePoolSize = ( (ThreadPoolExecutor) deleteExecutor ).getPoolSize();
        }
        else if ( deleteExecutor instanceof SingleThreadedExecutorService )
        {
            corePoolSize = 1;
        }

        for ( int i = 0; i < corePoolSize; i++ )
        {
            deleteExecutor.schedule( newTransferDeleteSweeper(), SWEEP_TIMEOUT_SECONDS, TimeUnit.SECONDS );
        }

        startReportingDaemon();
    }

    @PostConstruct
    public void startReportingDaemon()
    {
        startReporting();
    }

    @PreDestroy
    public void stopReportingDaemon()
    {
        stopReporting();
    }

    public boolean isFileBased()
    {
        return true;
    }

    @Override
    public File getDetachedFile( final ConcreteResource resource )
    {
        return new File( getFilePath( resource ) );
    }

    @Override
    public boolean isDirectory( final ConcreteResource resource )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.isDirectory( fileSystem, resource.getPath() );
    }

    @Override
    public boolean isFile( final ConcreteResource resource )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.isFile( fileSystem, resource.getPath() );
    }

    @Override
    public InputStream openInputStream( final ConcreteResource resource ) throws IOException
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.openInputStream( fileSystem, resource.getPath() );
    }

    @Override
    public OutputStream openOutputStream( final ConcreteResource resource ) throws IOException
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.openOutputStream( fileSystem, resource.getPath() );
    }

    @Override
    public boolean exists( final ConcreteResource resource )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.exists( fileSystem, resource.getPath() );
    }

    @Override
    public void copy( final ConcreteResource from, final ConcreteResource to ) throws IOException
    {
        Location loc = from.getLocation();
        String fromFileSystem = loc.getName();
        String fromPath = from.getPath();

        loc = to.getLocation();
        String toFileSystem = loc.getName();
        String toPath = to.getPath();

        fileManager.copy( fromFileSystem, fromPath, toFileSystem, toPath );
    }

    @Override
    public boolean delete( final ConcreteResource resource ) throws IOException
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.delete( fileSystem, resource.getPath() );
    }

    @Override
    public String[] list( final ConcreteResource resource )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.list( fileSystem, resource.getPath() );
    }

    @Override
    public void mkdirs( final ConcreteResource resource ) throws IOException
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        fileManager.makeDirs( fileSystem, resource.getPath() );
    }

    @Override
    public void createFile( final ConcreteResource resource ) throws IOException
    {
        throw new IOException( "createFile not supported" );
    }

    @Override
    public void createAlias( final ConcreteResource from, final ConcreteResource to ) throws IOException
    {
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
    public String getFilePath( final ConcreteResource resource )
    {
        return getStoragePath( resource );
    }

    @Override
    public String getStoragePath( final ConcreteResource resource )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.getFileStoragePath( fileSystem, resource.getPath() );
    }

    @Override
    public Transfer getTransfer( final ConcreteResource resource )
    {
        Transfer txfr = new Transfer( resource, this, fileEventManager, transferDecorator );
        String filePath = getFilePath( resource );
        if ( filePath == null )
        {
            logger.debug( "No storage filePath for {}", resource );
            return null;
        }

        File f = new File( filePath );
        final int timeoutSeconds = resource.getLocation()
                                           .getAttribute( Location.CACHE_TIMEOUT_SECONDS, Integer.class,
                                                          config.getDefaultTimeoutSeconds() );

        if ( !resource.isRoot() && f.exists() && !f.isDirectory() && config.isTimeoutProcessingEnabled()
                        && timeoutSeconds > 0 )
        {
            if ( isTimedOut( txfr, timeoutSeconds ) )
            {
                toDelete.add( txfr );
            }
        }

        return txfr;
    }

    private Runnable newTransferDeleteSweeper()
    {
        return () -> {
            if ( toDelete.isEmpty() )
            {
                return;
            }

            Transfer transfer = toDelete.remove( 0 );
            final int timeoutSeconds = transfer.getResource()
                                               .getLocation()
                                               .getAttribute( Location.CACHE_TIMEOUT_SECONDS, Integer.class,
                                                              config.getDefaultTimeoutSeconds() );

            if ( !transfer.getResource().isRoot() && transfer.exists() && !transfer.isDirectory()
                            && config.isTimeoutProcessingEnabled() && timeoutSeconds > 0 )
            {
                if ( isTimedOut( transfer, timeoutSeconds ) )
                {
                    ConcreteResource resource = transfer.getResource();
                    Location loc = resource.getLocation();
                    String fileSystem = loc.getName();
                    fileManager.delete( fileSystem, resource.getPath() );
                }
            }
        };
    }

    private boolean isTimedOut( final Transfer txfr, int timeoutSeconds )
    {
        final long current = System.currentTimeMillis();
        final long lastModified = txfr.lastModified();
        final int tos = timeoutSeconds < Location.MIN_CACHE_TIMEOUT_SECONDS ?
                        Location.MIN_CACHE_TIMEOUT_SECONDS :
                        timeoutSeconds;

        final long timeout = TimeUnit.MILLISECONDS.convert( tos, TimeUnit.SECONDS );

        return current - lastModified > timeout;
    }

    @Override
    public void clearTransferCache()
    {
        // do nothing
    }

    @Override
    public long length( final ConcreteResource resource )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.getFileLength( fileSystem, resource.getPath() );
    }

    @Override
    public long lastModified( final ConcreteResource resource )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.getFileLastModified( fileSystem, resource.getPath() );
    }

    @Override
    public boolean isReadLocked( final ConcreteResource resource )
    {
        return false;
    }

    @Override
    public boolean isWriteLocked( final ConcreteResource resource )
    {
        return false;
    }

    @Override
    public void unlockRead( final ConcreteResource resource )
    {
        // do nothing
    }

    @Override
    public void unlockWrite( final ConcreteResource resource )
    {
        // do nothing
    }

    @Override
    public void lockRead( final ConcreteResource resource )
    {
        // do nothing
    }

    @Override
    public void lockWrite( final ConcreteResource resource )
    {
        // do nothing
    }

    @Override
    public void waitForWriteUnlock( final ConcreteResource resource )
    {
        // do nothing
    }

    @Override
    public void waitForReadUnlock( final ConcreteResource resource )
    {
        // do nothing
    }

    @Override
    public AdminView asAdminView()
    {
        return this;
    }

    @Override
    public void cleanupCurrentThread()
    {
        fileManager.cleanupCurrentThread();
    }

    @Override
    public void startReporting()
    {
        fileManager.startReporting();
    }

    @Override
    public void stopReporting()
    {
        fileManager.stopReporting();
    }
}
