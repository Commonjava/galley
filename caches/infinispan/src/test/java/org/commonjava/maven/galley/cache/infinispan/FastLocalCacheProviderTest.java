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

import org.commonjava.maven.galley.cache.CacheProviderTCK;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FastLocalCacheProviderTest
        extends CacheProviderTCK
{
    private static EmbeddedCacheManager CACHE_MANAGER;

    private FastLocalCacheProvider provider;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    private final PathGenerator pathgen = new HashedLocationPathGenerator();

    private final FileEventManager events = new TestFileEventManager();

    private final TransferDecorator decorator = new TestTransferDecorator();

    private Cache<String, String> cache = CACHE_MANAGER.getCache( NFSOwnerCacheProducer.CACHE_NAME );

    private final ExecutorService executor = Executors.newFixedThreadPool( 5 );

    @BeforeClass
    public static void setupClass()
    {
        CACHE_MANAGER = new NFSOwnerCacheProducer().getCacheMgr();
    }

    @Before
    public void setup()
            throws Exception
    {
        final String nfsBasePath = createNFSBaseDir( temp.newFolder().getCanonicalPath() );
        provider =
                new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ),
                                            cache, pathgen, events, decorator, executor, nfsBasePath );
        provider.init();
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testConstructorWitNoNFSSysPath(){
        new FastLocalCacheProvider(  );
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testConstructorWitNoNFSSysPath2()throws IOException{
        final String NON_EXISTS_PATH = "/mnt/nfs/abc/xyz";
        System.setProperty( FastLocalCacheProvider.NFS_BASE_DIR_KEY, NON_EXISTS_PATH );
        new FastLocalCacheProvider(  );
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testConstructorWitNoNFSSysPath3()
            throws IOException
    {
        System.setProperty( FastLocalCacheProvider.NFS_BASE_DIR_KEY, temp.newFile().getCanonicalPath() );
        new FastLocalCacheProvider();
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testConstructorWitNoNFSSysPath4() throws IOException{
        new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ), cache,
                                    pathgen, events, decorator, executor );
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testConstructorWitNoNFSSysPath5() throws IOException{
        final String NON_EXISTS_PATH = "/mnt/nfs/abc/xyz";
        new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ), cache,
                                    pathgen, events, decorator, executor, NON_EXISTS_PATH);
    }

    @Test
    public void testConstructorWitNFSSysPath() throws IOException{
        System.setProperty( FastLocalCacheProvider.NFS_BASE_DIR_KEY, temp.newFolder().getCanonicalPath() );
        new FastLocalCacheProvider();
        new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ), cache,
                                    pathgen, events, decorator, executor );
    }

    @Override
    protected CacheProvider getCacheProvider()
            throws Exception
    {
        return provider;
    }

    private String createNFSBaseDir( String tempBaseDir ) throws IOException
    {
        File file = new File( tempBaseDir + "/mnt/nfs" );
        file.delete();
        file.mkdirs();
        return file.getCanonicalPath();
    }

    @After
    public void tearDown()
    {
        provider.destroy();
    }

}
