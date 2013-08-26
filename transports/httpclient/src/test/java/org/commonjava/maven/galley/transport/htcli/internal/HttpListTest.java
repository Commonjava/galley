package org.commonjava.maven.galley.transport.htcli.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.commonjava.maven.galley.TransferManagerImpl;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.transport.TransportManagerImpl;
import org.commonjava.maven.galley.transport.htcli.HttpClientTransport;
import org.commonjava.maven.galley.transport.htcli.model.SimpleHttpLocation;
import org.commonjava.maven.galley.transport.htcli.testutil.HttpTestFixture;
import org.junit.Rule;
import org.junit.Test;

public class HttpListTest
{
    static final String[] centralbtm = new String [] 
    {
    	"btm-2.1.3-javadoc.jar", 
    	"btm-2.1.3-javadoc.jar.asc", 
    	"btm-2.1.3-javadoc.jar.asc.md5", 
    	"btm-2.1.3-javadoc.jar.asc.sha1",
    	"btm-2.1.3-javadoc.jar.md5", 
    	"btm-2.1.3-javadoc.jar.sha1", 
    	"btm-2.1.3-sources.jar", 
    	"btm-2.1.3-sources.jar.asc", 
    	"btm-2.1.3-sources.jar.asc.md5", 
    	"btm-2.1.3-sources.jar.asc.sha1", 
    	"btm-2.1.3-sources.jar.md5", 
    	"btm-2.1.3-sources.jar.sha1", 
    	"btm-2.1.3-test-sources.jar", 
    	"btm-2.1.3-test-sources.jar.asc", 
    	"btm-2.1.3-test-sources.jar.asc.md5", 
    	"btm-2.1.3-test-sources.jar.asc.sha1", 
    	"btm-2.1.3-test-sources.jar.md5", 
    	"btm-2.1.3-test-sources.jar.sha1", 
    	"btm-2.1.3.jar", 
    	"btm-2.1.3.jar.asc", 
    	"btm-2.1.3.jar.asc.md5", 
    	"btm-2.1.3.jar.asc.sha1", 
    	"btm-2.1.3.jar.md5", 
    	"btm-2.1.3.jar.sha1", 
    	"btm-2.1.3.pom", 
    	"btm-2.1.3.pom.asc", 
    	"btm-2.1.3.pom.asc.md5", 
    	"btm-2.1.3.pom.asc.sha1", 
    	"btm-2.1.3.pom.md5", 
    	"btm-2.1.3.pom.sha1"
    };
    		
    static final String[] nexusswitchyard = new String[]
    {
    	"switchyard-runtime-1.0.0.Final-sources.jar", 
    	"switchyard-runtime-1.0.0.Final-sources.jar.md5", 
    	"switchyard-runtime-1.0.0.Final-sources.jar.sha1", 
    	"switchyard-runtime-1.0.0.Final-tests.jar", 
    	"switchyard-runtime-1.0.0.Final-tests.jar.md5", 
    	"switchyard-runtime-1.0.0.Final-tests.jar.sha1", 
    	"switchyard-runtime-1.0.0.Final.jar", 
    	"switchyard-runtime-1.0.0.Final.jar.md5", 
    	"switchyard-runtime-1.0.0.Final.jar.sha1", 
    	"switchyard-runtime-1.0.0.Final.pom", 
    	"switchyard-runtime-1.0.0.Final.pom.md5", 
    	"switchyard-runtime-1.0.0.Final.pom.sha1"
    };


    @Rule
    public HttpTestFixture fixture = new HttpTestFixture();

    @Test
    public void simpleCentralListing()
        throws Exception
    {
        final String fname = "btm/index.html";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, 5, null );
        final Transfer transfer = fixture.getCacheReference( new Resource( location, fname ) );

        HttpClientTransport hct = new HttpClientTransport (fixture.getHttp());
        TransportManager transportMgr = new TransportManagerImpl( hct );
        MemoryNotFoundCache nfc = new MemoryNotFoundCache();
        FileEventManager fileEvents = new NoOpFileEventManager();
        TransferDecorator decorator = new NoOpTransferDecorator();
        ExecutorService executor= Executors.newSingleThreadExecutor();

        TransferManagerImpl mgr = new TransferManagerImpl( transportMgr, fixture.getCache(), nfc, fileEvents, decorator, executor );

        assertThat( transfer.exists(), equalTo( false ) );

        String[] listing = mgr.list(new Resource(location,"") ).getListing();
      
//        System.out.println ("### " + Arrays.toString(listing));

        assertTrue (Arrays.equals(centralbtm, listing));
        
   }

    @Test
    public void simpleNexusListing()
        throws Exception
    {
        final String fname = "switchyard/index.html";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, 5, null );
        final Transfer transfer = fixture.getCacheReference( new Resource( location, fname ) );

        HttpClientTransport hct = new HttpClientTransport (fixture.getHttp());
        TransportManager transportMgr = new TransportManagerImpl( hct );
        MemoryNotFoundCache nfc = new MemoryNotFoundCache();
        FileEventManager fileEvents = new NoOpFileEventManager();
        TransferDecorator decorator = new NoOpTransferDecorator();
        ExecutorService executor= Executors.newSingleThreadExecutor();

        TransferManagerImpl mgr = new TransferManagerImpl( transportMgr, fixture.getCache(), nfc, fileEvents, decorator, executor );

        assertThat( transfer.exists(), equalTo( false ) );

        String[] listing = mgr.list(new Resource(location,"") ).getListing();
       
        // System.out.println ("### " + Arrays.toString(listing));

        assertTrue (Arrays.equals(nexusswitchyard, listing));
    }
}
