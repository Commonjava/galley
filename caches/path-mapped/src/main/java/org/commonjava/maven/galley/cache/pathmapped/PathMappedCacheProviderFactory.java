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

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.cache.CacheProviderFactory;
import org.commonjava.maven.galley.io.SpecialPathManagerImpl;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.commonjava.storage.pathmapped.config.PathMappedStorageConfig;
import org.commonjava.storage.pathmapped.core.FileBasedPhysicalStore;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.storage.pathmapped.pathdb.datastax.CassandraPathDB;
import org.commonjava.storage.pathmapped.spi.PathDB;
import org.commonjava.storage.pathmapped.spi.PhysicalStore;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class PathMappedCacheProviderFactory
                implements CacheProviderFactory
{
    private final File cacheDir;

    private final PathMappedStorageConfig config;

    private final ExecutorService deleteExecutor;

    private transient PathMappedCacheProvider provider;

    private PathDB pathDB;

    private PhysicalStore physicalStore;

    private SpecialPathManager specialPathManager;

    private PathMappedCacheProviderConfig cacheProviderConfig;

    public PathMappedCacheProviderFactory( File cacheDir, ExecutorService deleteExecutor,
                                           PathMappedStorageConfig config )
    {
        this( cacheDir, deleteExecutor, config, null, null, null );
    }

    public PathMappedCacheProviderFactory( File cacheDir, ExecutorService deleteExecutor,
                                           PathMappedStorageConfig config, PathDB pathDB, PhysicalStore physicalStore,
                                           PathMappedCacheProviderConfig cacheProviderConfig )
    {
        this.cacheDir = cacheDir;
        this.deleteExecutor = deleteExecutor;
        this.config = config;
        this.pathDB = pathDB;
        this.physicalStore = physicalStore;
        this.cacheProviderConfig = cacheProviderConfig;
    }

    @Override
    public synchronized CacheProvider create( PathGenerator pathGenerator, TransferDecoratorManager transferDecorator,
                                              FileEventManager fileEventManager ) throws GalleyInitException
    {
        if ( provider == null )
        {
            try
            {
                if ( pathDB == null )
                {
                    pathDB = new CassandraPathDB( config );
                }
                if ( physicalStore == null )
                {
                    physicalStore = new FileBasedPhysicalStore( cacheDir );
                }
                if ( specialPathManager == null )
                {
                    specialPathManager = new SpecialPathManagerImpl();
                }
                if ( cacheProviderConfig == null )
                {
                    cacheProviderConfig = new PathMappedCacheProviderConfig( cacheDir );
                }
                provider = new PathMappedCacheProvider( cacheDir, fileEventManager, transferDecorator, cacheProviderConfig, deleteExecutor,
                                                        new PathMappedFileManager( config, pathDB, physicalStore ),
                                                        pathGenerator, specialPathManager );
            }
            catch ( Exception ex )
            {
                throw new GalleyInitException( "Create cache provider failed", ex );
            }
        }
        return provider;
    }
}
