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
package org.commonjava.maven.galley.cache.partyline;

import org.commonjava.maven.galley.cache.CacheProviderTCK;
import org.commonjava.maven.galley.cache.infinispan.GridFileSystemCacheProvider;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.infinispan.Cache;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

public class GridFileSystemCacheProviderTest
        extends CacheProviderTCK
{

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    private GridFileSystemCacheProvider provider;

    private static EmbeddedCacheManager CACHE_MANAGER;

    @BeforeClass
    public static void setupClass()
    {
        CACHE_MANAGER = new DefaultCacheManager( new GlobalConfigurationBuilder().build() );
    }

    @Before
    public void setup()
            throws Exception
    {
        final PathGenerator pathgen = new HashedLocationPathGenerator();
        final FileEventManager events = new TestFileEventManager();
        final TransferDecorator decorator = new TestTransferDecorator();

        Cache<String, byte[]> data = CACHE_MANAGER.getCache( name.getMethodName() + "-data" );
        Cache<String, GridFile.Metadata> metadata = CACHE_MANAGER.getCache( name.getMethodName() + "-metadata" );

        final GridFilesystem fs = new GridFilesystem( data, metadata );

        provider = new GridFileSystemCacheProvider( pathgen, events, decorator, fs );
    }

    @Override
    protected CacheProvider getCacheProvider()
            throws Exception
    {
        return provider;
    }
}
