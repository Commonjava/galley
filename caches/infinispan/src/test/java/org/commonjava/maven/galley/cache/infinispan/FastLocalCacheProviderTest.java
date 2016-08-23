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

    @BeforeClass
    public static void setupClass()
    {
        CACHE_MANAGER = new NFSOwnerCacheProducer().getCacheMgr();
    }

    @Before
    public void setup()
            throws Exception
    {
        final PathGenerator pathgen = new HashedLocationPathGenerator();
        final FileEventManager events = new TestFileEventManager();
        final TransferDecorator decorator = new TestTransferDecorator();

        Cache<String, String> cache = CACHE_MANAGER.getCache( NFSOwnerCacheProducer.CACHE_NAME );

        final String nfsBasePath = createNFSBaseDir( temp.newFolder().getAbsolutePath() );

        final Executor executor = Executors.newFixedThreadPool( 5 );
        provider =
                new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ),
                                            cache, events, decorator, executor, nfsBasePath );
        provider.init();
    }

    @Test
    @BMScript( "TryToReadWhileWritingTestCase.btm" )
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

    @Override
    protected CacheProvider getCacheProvider()
            throws Exception
    {
        return provider;
    }

    private String createNFSBaseDir( String tempBaseDir )
    {
        File file = new File( tempBaseDir + "/mnt/nfs" );
        file.delete();
        file.mkdir();
        return file.getAbsolutePath();
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
