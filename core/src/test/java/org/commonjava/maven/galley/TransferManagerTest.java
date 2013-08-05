package org.commonjava.maven.galley;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.commonjava.maven.galley.cache.CacheProvider;
import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.event.FileEventManager;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashPerRepoPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.io.TransferDecorator;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.testutil.TestDownload;
import org.commonjava.maven.galley.testutil.TestTransport;
import org.commonjava.maven.galley.transport.SimpleTransportManager;
import org.commonjava.maven.galley.transport.TransportManager;
import org.commonjava.util.logging.Log4jUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for the {@link TransferManagerImpl} itself. As far as possible, uses 
 * stubbed-out infrastructure components to isolate the behavior or this manager 
 * component ONLY, and avoid testing other component implementations in this 
 * class.
 * 
 * @author jdcasey
 */
public class TransferManagerTest
{

    private TransferManager mgr;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private TransportManager transportMgr;

    private CacheProvider cacheProvider;

    private FileEventManager fileEvents;

    private TransferDecorator decorator;

    private ExecutorService executor;

    private TestTransport transport;

    @BeforeClass
    public static void setupLogging()
    {
        Log4jUtil.configure( Level.DEBUG );
    }

    @Before
    public void setup()
    {
        transport = new TestTransport();
        transportMgr = new SimpleTransportManager( transport );
        cacheProvider = new FileCacheProvider( temp.newFolder( "cache" ), new HashPerRepoPathGenerator() );
        fileEvents = new NoOpFileEventManager();
        decorator = new NoOpTransferDecorator();
        executor = Executors.newSingleThreadExecutor();

        mgr = new TransferManagerImpl( transportMgr, cacheProvider, fileEvents, decorator, executor );
    }

    /**
     * Test that cached content will be used...if not, this test will fail, as 
     * the wrong content is registered with the test transport.
     */
    @Test
    public void retrieve_preferCachedCopy()
        throws Exception
    {
        final String testContent = "This is a test " + System.currentTimeMillis();

        final Location loc = new SimpleLocation( "file:///test-repo" );
        final String path = "/path/to/test.txt";

        // put in some wrong content that will cause problems if the cache isn't used.
        transport.registerDownload( loc, path, new TestDownload( "This is WRONG".getBytes() ) );

        // seed the cache with the file we're trying to retrieve.
        OutputStream os = null;
        try
        {
            os = cacheProvider.openOutputStream( loc, path );
            os.write( testContent.getBytes() );
        }
        finally
        {
            closeQuietly( os );
        }

        // now, use the manager to retrieve() the path...the cached content should come through here.
        final Transfer transfer = mgr.retrieve( loc, path );

        assertTransferContent( transfer, testContent );
    }

    /**
     * Test that remote content will be downloaded then cached.
     */
    @Test
    public void retrieve_cacheIfMissing()
        throws Exception
    {
        final String testContent = "This is a test " + System.currentTimeMillis();

        final Location loc = new SimpleLocation( "file:///test-repo" );
        final String path = "/path/to/test.txt";

        // put in the content that we want to "download"
        transport.registerDownload( loc, path, new TestDownload( testContent.getBytes() ) );

        // now, use the manager to retrieve() the path...the remote content should come through here.
        Transfer transfer = mgr.retrieve( loc, path );

        assertTransferContent( transfer, testContent );

        // now, the right content should be cached.
        // So, we'll put in some wrong content that will cause problems if the cache isn't used.
        transport.registerDownload( loc, path, new TestDownload( "This is WRONG".getBytes() ) );

        // now, use the manager to retrieve() the path again...the cached content should come through here.
        transfer = mgr.retrieve( loc, path );

        assertTransferContent( transfer, testContent );
    }

    private void assertTransferContent( final Transfer transfer, final String testContent )
        throws Exception
    {
        // if this is null, it's a sign that the manager tried to retrieve the content remotely and ignored the cache.
        assertThat( transfer, notNullValue() );

        // now, read the content to verify it matches what we wrote above.
        InputStream is = null;
        try
        {
            is = transfer.openInputStream();
            final String result = IOUtils.toString( is );

            assertThat( result, equalTo( testContent ) );
        }
        finally
        {
            closeQuietly( is );
        }
    }

}
