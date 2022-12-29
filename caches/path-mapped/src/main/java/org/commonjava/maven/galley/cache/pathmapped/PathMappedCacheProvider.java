/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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

import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.commonjava.maven.galley.util.PathUtils;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.commonjava.maven.galley.io.SpecialPathConstants.PKG_TYPE_MAVEN;
import static org.commonjava.maven.galley.io.SpecialPathConstants.PKG_TYPE_NPM;

public class PathMappedCacheProvider
                implements CacheProvider, CacheProvider.AdminView
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final PathMappedCacheProviderConfig config;

    private final FileEventManager fileEventManager;

    private final TransferDecoratorManager transferDecorator;

    private final PathMappedFileManager fileManager;

    private final PathGenerator pathGenerator;

    private final SpecialPathManager specialPathManager;

    private static final int DEFAULT_DELETE_EXECUTOR_POOL_SIZE = 2;

    public PathMappedCacheProvider( final File cacheBasedir,
                                    final FileEventManager fileEventManager,
                                    final TransferDecoratorManager transferDecorator,
                                    final PathMappedCacheProviderConfig pathMappedCacheProviderConfig,
                                    final ExecutorService deleteExecutor,
                                    final PathMappedFileManager fileManager,
                                    final PathGenerator pathGenerator,
                                    final SpecialPathManager specialPathManager)
    {
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.config = pathMappedCacheProviderConfig == null ?
                        new PathMappedCacheProviderConfig( cacheBasedir ) :
                        pathMappedCacheProviderConfig;
        ExecutorService deleteExecutor1 =
                deleteExecutor == null ? newFixedThreadPool( DEFAULT_DELETE_EXECUTOR_POOL_SIZE ) : deleteExecutor;
        this.fileManager = fileManager;
        this.pathGenerator = pathGenerator;
        this.specialPathManager = specialPathManager;
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

    /**
     * Only for test.
     */
    @Override
    public File getDetachedFile( final ConcreteResource resource )
    {
        return handleResource( resource,
                               ( f, p ) -> new File( config.getCacheBasedir(), fileManager.getFileStoragePath( f, p ) ),
                               "getDetachedFile" );
    }

    @Override
    public void gc()
    {
        fileManager.gc();
    }

    @Override
    public void close()
    {
        try
        {
            fileManager.close();
        }
        catch ( IOException e )
        {
            logger.warn( "Fail to close", e );
        }
    }

    @Override
    public boolean isDirectory( final ConcreteResource resource )
    {
        return handleResource( resource, fileManager::isDirectory, "isDirectory" );
    }

    @Override
    public boolean isFile( final ConcreteResource resource )
    {
        return handleResource( resource, fileManager::isFile, "isFile" );
    }

    @Override
    public InputStream openInputStream( final ConcreteResource resource ) throws IOException
    {
        return handleResourceIO( resource, fileManager::openInputStream );
    }

    @Override
    public OutputStream openOutputStream( final ConcreteResource resource ) throws IOException
    {
        return handleResourceIO( resource, fileManager::openOutputStream );
    }

    @Override
    public boolean exists( final ConcreteResource resource )
    {
        return handleResource( resource, fileManager::exists, "exists" );
    }

    @Override
    public void copy( final ConcreteResource from, final ConcreteResource to )
    {
        Location loc = from.getLocation();
        String fromFileSystem = loc.getName();
        String fromPath = pathGenerator.getPath( from );

        loc = to.getLocation();
        String toFileSystem = loc.getName();
        String toPath = pathGenerator.getPath( to );

        fileManager.copy( fromFileSystem, fromPath, toFileSystem, toPath );
    }

    @Override
    public boolean delete( final ConcreteResource resource )
    {
        return handleResource( resource, fileManager::delete, "delete" );
    }

    @Override
    public String[] list( final ConcreteResource resource )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        return fileManager.list( fileSystem, resource.getPath() );
    }

    @Override
    public void mkdirs( final ConcreteResource resource )
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
    public void createAlias( final ConcreteResource from, final ConcreteResource to )
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

    /**
     * Logic file path, e.g., maven/remote-foo/bar/package.json
     */
    @Override
    public String getFilePath( final ConcreteResource resource )
    {
        return handleResource( resource, PathUtils::normalize, "getFilePath" );
    }

    /**
     * Logic storage path, e.g., for NPM, the bar/package.json is the storage path while the raw path is bar/
     */
    @Override
    public String getStoragePath( final ConcreteResource resource )
    {
        return handleResource( resource, ( f, p ) -> p, "getStoragePath" );
    }

    @Override
    public Transfer getTransfer( final ConcreteResource resource )
    {
        Transfer txfr = new Transfer( resource, this, fileEventManager, transferDecorator );
        if ( !resource.isRoot() && config.isTimeoutProcessingEnabled() )
        {
            if ( isTransferTimeout( txfr ) )
            {

                logger.info("Removing resource {} as timeout.", resource);

                handleResource( resource, fileManager::delete, "transferDelete" );
            }
        }
        return txfr;
    }

    protected int getResourceTimeoutSeconds( final ConcreteResource resource )
    {
        return resource.getLocation()
                       .getAttribute( Location.CACHE_TIMEOUT_SECONDS, Integer.class,
                                      config.getDefaultTimeoutSeconds() );
    }

    protected int getResourceMetadataTimeoutSeconds( final ConcreteResource resource )
    {
        return resource.getLocation()
                .getAttribute( Location.METADATA_TIMEOUT_SECONDS, Integer.class,
                        Location.DEFAULT_CACHE_TIMEOUT_SECONDS );
    }

    /**
     * Return true if it is both a file and timeout. False if file not exists or directory. This is because
     * getFileLastModified return -1 when file not exists or directory.
     */
    protected boolean isTransferTimeout( final Transfer txfr )
    {
        int timeoutSeconds = 0;
        SpecialPathInfo pathInfo = null;

        for ( String pkgType : Arrays.asList( PKG_TYPE_MAVEN, PKG_TYPE_NPM ) )
        {
            /*
             * this returns the 'real' path, eg., appending the 'package.json' for npm metadata.
             */
            String realPath = pathGenerator.getPath( txfr.getResource() );
            pathInfo = specialPathManager.getSpecialPathInfo( txfr.getLocation(), realPath, pkgType );
            if ( pathInfo != null && pathInfo.isMetadata() )
            {
                timeoutSeconds = getResourceMetadataTimeoutSeconds( txfr.getResource() );
                logger.debug("isTransferTimeout, pkgType: {}, metadata timeoutSeconds: {}, realPath: {}",
                        pkgType, timeoutSeconds, realPath);
                break;
            }
        }
        if ( pathInfo == null || !pathInfo.isMetadata() )
        {
            timeoutSeconds = getResourceTimeoutSeconds( txfr.getResource() );
            logger.debug("isTransferTimeout, resource timeoutSeconds: {}", timeoutSeconds);
        }

        if ( timeoutSeconds <= 0 )
        {
            return false;
        }

        final long current = System.currentTimeMillis();
        final long lastModified = txfr.lastModified();
        if ( lastModified <= 0 )
        {
            // not exist or not a file
            logger.debug("isTransferTimeout, lastModified: {}", lastModified);
            return false;
        }

        // final int tos = Math.max( timeoutSeconds, Location.MIN_CACHE_TIMEOUT_SECONDS );
        /*
         * I disrespect the MIN_CACHE_TIMEOUT_SECONDS because it blocks the testing and confuses users
         * when setting timeout smaller than 1h. ruhan May 30, 2022
         */
        final int tos = timeoutSeconds;
        final long timeout = TimeUnit.MILLISECONDS.convert( tos, TimeUnit.SECONDS );
        logger.debug("isTransferTimeout, tos: {}, timeout: {}, current: {}, lastModified: {}", tos, timeout, current, lastModified);
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
        return handleResource( resource, fileManager::getFileLength, "length" );
    }

    @Override
    public long lastModified( final ConcreteResource resource )
    {
        return handleResource( resource, fileManager::getFileLastModified, "lastModified" );
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
    }

    @Override
    public void startReporting()
    {
    }

    @Override
    public void stopReporting()
    {
    }

    public PathMappedFileManager getPathMappedFileManager()
    {
        return fileManager;
    }

    @FunctionalInterface
    interface PathMappedPathHandler<T>
    {
        T handlePath( String fileSystem, String path );
    }

    // handlerName for debug and accounting
    private <T> T handleResource( ConcreteResource resource, PathMappedPathHandler<T> handler, String handlerName )
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        String realPath = pathGenerator.getPath( resource );
        logger.debug( "handleResource, fileSystem:{}, realPath:{}, handler:{}", fileSystem, realPath, handlerName );
        return handler.handlePath( fileSystem, realPath );
    }

    @FunctionalInterface
    interface PathMappedPathIOHandler<T>
    {
        T handlePath( String fileSystem, String path )
                throws IOException;
    }

    private <T> T handleResourceIO( ConcreteResource resource, PathMappedPathIOHandler<T> handler )
            throws IOException
    {
        Location loc = resource.getLocation();
        String fileSystem = loc.getName();
        String realPath = pathGenerator.getPath( resource );
        return handler.handlePath( fileSystem, realPath );
    }
}
