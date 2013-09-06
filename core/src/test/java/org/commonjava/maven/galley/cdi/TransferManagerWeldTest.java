package org.commonjava.maven.galley.cdi;

import org.commonjava.maven.galley.AbstractTransferManagerTest;
import org.commonjava.maven.galley.TransferManagerImpl;
import org.commonjava.maven.galley.cache.FileCacheProviderConfig;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.testing.core.cdi.ApiCDIExtension;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.commonjava.util.logging.Logger;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
@Ignore
public class TransferManagerWeldTest
    extends AbstractTransferManagerTest
{

    private Weld weld;

    private WeldContainer weldContainer;

    private TransferManagerImpl mgr;

    private CacheProvider cacheProvider;

    private TestTransport transport;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void setup()
    {
        weld = new Weld();

        weld.addExtension( new ApiCDIExtension().withDefaultComponentInstances()
                                                .withDefaultBeans()
                                                .withDefaultBean( new FileCacheProviderConfig( temp.newFolder( "cache" ) ),
                                                                  FileCacheProviderConfig.class ) );
        weldContainer = weld.initialize();

        transport = weldContainer.instance()
                                 .select( TestTransport.class )
                                 .get();

        new Logger( getClass() ).info( "Got transport: %s", transport );

        cacheProvider = weldContainer.instance()
                                     .select( CacheProvider.class )
                                     .get();

        mgr = weldContainer.instance()
                           .select( TransferManagerImpl.class )
                           .get();
    }

    @After
    public void shutdown()
    {
        if ( weldContainer != null )
        {
            weld.shutdown();
        }
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
