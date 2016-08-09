package org.commonjava.maven.galley.cache;

import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

@RunWith( org.jboss.byteman.contrib.bmunit.BMUnitRunner.class )
@BMUnitConfig( loadDirectory = "target/test-classes/bmunit", debug = true )
public class FileCacheBMUnitTest
{

    final String content = "This is a bmunit test";

    final Location loc = new SimpleLocation( "http://foo.com" );

    final String fname = "/path/to/my/file.txt";

    final CountDownLatch latch = new CountDownLatch( 2 );

    final ConcreteResource resource = new ConcreteResource( loc, fname );

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    final CacheProvider provider = getCacheProvider();

    String result = "";

    public FileCacheBMUnitTest()
            throws Exception
    {
    }

    public CacheProvider getCacheProvider()
            throws Exception
    {
        temp.create();
        return new FileCacheProvider( temp.newFolder( "cache" ), new HashedLocationPathGenerator(),
                                      new NoOpFileEventManager(), new NoOpTransferDecorator(), true );
    }

    @Test
    public void testSimultaneousWriteReadThreadLockConsistence()
            throws Exception
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
    @BMRule( name = "test-simultaneous-write-read-without-await-all",
             targetClass = "CountDownLatch",
             targetMethod = "await",
             action = "throw new java.lang.RuntimeException()" )
    public void testSimultaneousWriteReadThreadLockWithoutAwaitAll()
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
        assertNotSame( result, content );
    }

    @Ignore
    @Test
    @BMRule( name = "test-simultaneous-write-read-with-invalid-lock",
             targetClass = "FileCacheProvider",
             targetMethod = "openOutputStream(ConcreteResource)",
             action = "throw new java.lang.RuntimeException()" )
    public void testSimultaneousWriteReadThreadWithInvalidLock()
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
        assertNotSame( result, content );
    }

    class WriteThread
            implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                provider.lockRead( resource );
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
                System.out.println( e );
            }
            finally
            {
                provider.unlockRead( resource );
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
                System.out.println( e );
            }
        }
    }
}
