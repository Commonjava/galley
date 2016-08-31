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
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 */
@RunWith( org.jboss.byteman.contrib.bmunit.BMUnitRunner.class )
@BMUnitConfig( loadDirectory = "target/test-classes/bmunit", debug = true )
public class FastLocalCacheProviderTest
        extends CacheProviderTCK
{
    private static EmbeddedCacheManager CACHE_MANAGER;

    final String content = "This is a bmunit test";

    final Location loc = new SimpleLocation( "http://foo.com" );

    final String fname = "/path/to/my/file.txt";

    final CountDownLatch latch = new CountDownLatch( 2 );

    final ConcreteResource resource = new ConcreteResource( loc, fname );

    private FastLocalCacheProvider provider;

    private String result = null;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    private final PathGenerator pathgen = new HashedLocationPathGenerator();

    private final FileEventManager events = new TestFileEventManager();

    private final TransferDecorator decorator = new TestTransferDecorator();

    private Cache<String, String> cache = CACHE_MANAGER.getCache( NFSOwnerCacheProducer.CACHE_NAME );

    private final Executor executor = Executors.newFixedThreadPool( 5 );

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

    @Test
    @BMScript( "TryToReadWhileWritingTestCase.btm" )
    @Ignore( "Needs to be in a separate class to avoid BindException on byteman JVM agent")
    public void testTryToReadWhileWriting()
            throws IOException, InterruptedException
    {
        new Thread( new WriteThread() ).start();
        new Thread( new ReadThread() ).start();
        try
        {
            latch.await();
        }
        catch ( Exception e )
        {
            System.out.println( "Threads await Exception." );
        }
        assertThat( result, equalTo( content ) );
    }

    @Test
    @BMScript( "TryToWriteWhileReadingWithFileExistedTestCase.btm" )
    @Ignore( "Needs to be in a separate class to avoid BindException on byteman JVM agent")
    public void testTryToWriteWhileReadingWithFileExisted()
            throws IOException, InterruptedException
    {
        new Thread( new ReadThread() ).start();
        new Thread( new WriteThread() ).start();
        try
        {
            latch.await();
        }
        catch ( Exception e )
        {
            System.out.println( "Threads await Exception." );
        }
        assertNull( result );
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

    class WriteThread
            implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                final OutputStream out = provider.openOutputStream( resource );
                final ByteArrayInputStream bais = new ByteArrayInputStream( content.getBytes() );
                int read = -1;
                final byte[] buf = new byte[512];
                while ( ( read = bais.read( buf ) ) > -1 )
                {
                    Thread.sleep( 1000 );
                    out.write( buf, 0, read );
                }
                out.close();
                latch.countDown();
            }
            catch ( Exception e )
            {
                System.out.println( "Write Thread Runtime Exception." );
                e.printStackTrace();
            }
        }
    }

    class ReadThread
            implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                final InputStream in = provider.openInputStream( resource );
                if ( in == null )
                {
                    latch.countDown();
                    return;
                }
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int read = -1;
                final byte[] buf = new byte[512];
                while ( ( read = in.read( buf ) ) > -1 )
                {
                    baos.write( buf, 0, read );
                }
                result = new String( baos.toByteArray(), "UTF-8" );
                latch.countDown();
            }
            catch ( Exception e )
            {
                System.out.println( "Read Thread Runtime Exception." );
                e.printStackTrace();
            }
        }
    }
}
