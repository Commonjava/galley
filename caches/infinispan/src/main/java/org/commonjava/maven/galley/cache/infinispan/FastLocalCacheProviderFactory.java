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
package org.commonjava.maven.galley.cache.infinispan;

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.cache.CacheProviderFactory;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * Created by jdcasey on 8/30/16.
 */
@Deprecated
public class FastLocalCacheProviderFactory
        implements CacheProviderFactory
{
    private File cacheDir;

    private final File nfsDir;

    private CacheInstance<String, String> nfsUsageCache;

    private final ExecutorService executor;

    private transient FastLocalCacheProvider provider;

    private final CacheInstance<String, ConcreteResource> localFilePathCache;

    public FastLocalCacheProviderFactory( File cacheDir, File nfsDir, CacheInstance<String, String> nfsUsageCache,
                                          CacheInstance<String, ConcreteResource> localFilePathCache,
                                          ExecutorService executor )
    {
        this.cacheDir = cacheDir;
        this.nfsDir = nfsDir;
        this.nfsUsageCache = nfsUsageCache;
        this.executor = executor;
        this.localFilePathCache = localFilePathCache;
    }

    public FastLocalCacheProviderFactory( File cacheDir, File nfsDir, CacheInstance<String, String> nfsUsageCache,
                                          ExecutorService executor )
    {
        this.cacheDir = cacheDir;
        this.nfsDir = nfsDir;
        this.nfsUsageCache = nfsUsageCache;
        this.executor = executor;
        this.localFilePathCache = new SimpleCacheInstance<>( NFSOwnerCacheProducer.DEFAULT_LOCAL_CACHE_FILE_NAME,
                                                             new NFSOwnerCacheProducer().getCacheMgr()
                                                                                        .getCache(
                                                                                                NFSOwnerCacheProducer.DEFAULT_LOCAL_CACHE_FILE_NAME ) );
    }

    @Override
    public synchronized CacheProvider create( PathGenerator pathGenerator, TransferDecorator transferDecorator,
                                 FileEventManager fileEventManager )
            throws GalleyInitException
    {
        if ( provider == null )
        {
            PartyLineCacheProvider pl =
                    new PartyLineCacheProvider( cacheDir, pathGenerator, fileEventManager, transferDecorator );

            provider =
                    new FastLocalCacheProvider( pl, nfsUsageCache, pathGenerator, fileEventManager, transferDecorator,
                                                executor, nfsDir.getPath(), localFilePathCache );
        }

        return provider;
    }
}
