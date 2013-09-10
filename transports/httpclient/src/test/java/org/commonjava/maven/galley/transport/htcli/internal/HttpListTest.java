package org.commonjava.maven.galley.transport.htcli.internal;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.transport.htcli.model.SimpleHttpLocation;
import org.commonjava.maven.galley.transport.htcli.testutil.HttpTestFixture;
import org.junit.Rule;
import org.junit.Test;

public class HttpListTest
{
    /* @formatter:off */
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
    /* @formatter:on */

    @Rule
    public HttpTestFixture fixture = new HttpTestFixture( "list-basic" );

    @Test
    public void simpleCentralListing()
        throws Exception
    {
        final String fname = "central-btm/index.html";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, 5, null );

        final HttpListing listing = new HttpListing( url, new ConcreteResource( location, fname ), 10, fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( listing.getError(), nullValue() );
        assertTrue( Arrays.equals( centralbtm, result.getListing() ) );
    }

    @Test
    public void simpleCentralListing_Missing()
        throws Exception
    {
        final String fname = "central-missing/index.html";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, 5, null );

        final HttpListing listing = new HttpListing( url, new ConcreteResource( location, fname ), 10, fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( result, nullValue() );
        assertThat( listing.getError(), nullValue() );
    }

    @Test
    public void simpleCentralListing_Error()
        throws Exception
    {
        final String fname = "central-error/index.html";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, 5, null );

        fixture.registerException( fixture.getUrlPath( url ), "Test Error" );

        final HttpListing listing = new HttpListing( url, new ConcreteResource( location, fname ), 10, fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( result, nullValue() );
        assertThat( listing.getError(), notNullValue() );
        assertTrue( listing.getError()
                           .getMessage()
                           .endsWith( "Test Error" ) );
    }

    @Test
    public void simpleNexusListing()
        throws Exception
    {
        final String fname = "nexus-switchyard/index.html";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, 5, null );
        final HttpListing listing = new HttpListing( url, new ConcreteResource( location, fname ), 10, fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( listing.getError(), nullValue() );
        assertTrue( Arrays.equals( nexusswitchyard, result.getListing() ) );
    }

    @Test
    public void simpleNexusListing_Missing()
        throws Exception
    {
        final String fname = "nexus-missing/index.html";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, 5, null );
        final HttpListing listing = new HttpListing( url, new ConcreteResource( location, fname ), 10, fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( listing.getError(), nullValue() );
        assertThat( result, nullValue() );
    }

    @Test
    public void simpleNexusListing_Error()
        throws Exception
    {
        final String fname = "nexus-error/index.html";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, 5, null );

        fixture.registerException( fixture.getUrlPath( url ), "Test Error" );

        final HttpListing listing = new HttpListing( url, new ConcreteResource( location, fname ), 10, fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( result, nullValue() );
        assertThat( listing.getError(), notNullValue() );
        assertTrue( listing.getError()
                           .getMessage()
                           .endsWith( "Test Error" ) );
    }
}
