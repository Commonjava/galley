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

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.cache.CacheProviderFactory;
import org.commonjava.storage.pathmapped.config.PathMappedStorageConfig;
import org.commonjava.storage.pathmapped.datastax.CassandraPathDB;
import org.commonjava.storage.pathmapped.core.FileBasedPhysicalStore;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class PathMappedCacheProviderFactory
                implements CacheProviderFactory
{
    private File cacheDir;

    private PathMappedStorageConfig config;

    private ExecutorService deleteExecutor;

    private transient PathMappedCacheProvider provider;

    public PathMappedCacheProviderFactory( File cacheDir, ExecutorService deleteExecutor,
                                           PathMappedStorageConfig config )
    {
        this.cacheDir = cacheDir;
        this.deleteExecutor = deleteExecutor;
        this.config = config;
    }

    @Override
    public synchronized CacheProvider create( PathGenerator pathGenerator, TransferDecoratorManager transferDecorator,
                                              FileEventManager fileEventManager ) throws GalleyInitException
    {
        if ( provider == null )
        {
            try
            {
                provider = new PathMappedCacheProvider( cacheDir, fileEventManager, transferDecorator, deleteExecutor,
                                                        new PathMappedFileManager( config,
                                                                                   new CassandraPathDB( config ),
                                                                                   new FileBasedPhysicalStore(
                                                                                                   cacheDir ) ),
                                                        pathGenerator );
            }
            catch ( Exception ex )
            {
                throw new GalleyInitException( "Create cache provider failed", ex );
            }
        }
        return provider;
    }
}
