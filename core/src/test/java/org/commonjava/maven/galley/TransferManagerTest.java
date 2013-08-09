package org.commonjava.maven.galley;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.testutil.TestTransport;
import org.commonjava.maven.galley.transport.TransportManagerImpl;
import org.junit.Before;
import org.junit.Rule;
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
    extends AbstractTransferManagerTest
{

    private TransferManagerImpl mgr;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private TransportManager transportMgr;

    private CacheProvider cacheProvider;

    private FileEventManager fileEvents;

    private TransferDecorator decorator;

    private ExecutorService executor;

    private TestTransport transport;

    @Before
    public void setup()
    {
        transport = new TestTransport();
        transportMgr = new TransportManagerImpl( transport );
        cacheProvider = new FileCacheProvider( temp.newFolder( "cache" ), new HashedLocationPathGenerator() );
        fileEvents = new NoOpFileEventManager();
        decorator = new NoOpTransferDecorator();
        executor = Executors.newSingleThreadExecutor();

        mgr = new TransferManagerImpl( transportMgr, cacheProvider, fileEvents, decorator, executor );
    }

    @Override
    protected TransferManagerImpl getTransferManagerImpl()
        throws Exception
    {
        return mgr;
    }

    @Override
    protected TestTransport getTransport()
        throws Exception
    {
        return transport;
    }

    @Override
    protected CacheProvider getCacheProvider()
        throws Exception
    {
        return cacheProvider;
    }

}
