/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.transport.htcli.internal;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Transfer;
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
        final String dir = "central-btm/";
        final String fname = dir + "index.html";
        final String listingFname = dir + ".listing.txt";

        final String url = fixture.formatUrl( fname );
        final String body = getBody( fname );
        fixture.getServer()
               .expect( url, 200, body );

        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, listingFname ) );

        final HttpListing listing =
            new HttpListing( url, new ConcreteResource( location, fname ), fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( listing.getError(), nullValue() );
        assertThat( result, notNullValue() );
        assertThat( result.getListing(), notNullValue() );

        System.out.println( "Got listing\n\n  " + StringUtils.join( result.getListing(), "\n  " ) + "\n\n" );
        assertTrue( Arrays.equals( centralbtm, result.getListing() ) );
    }

    private String getBody( final String fname )
        throws Exception
    {
        final InputStream stream = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream( "list-basic/" + fname );

        return IOUtils.toString( stream );
    }

    @Test
    public void simpleCentralListing_WriteListingFile()
        throws Exception
    {
        final String dir = "central-btm/";
        final String fname = dir + "index.html";
        final String listingFname = dir + ".listing.txt";

        final String url = fixture.formatUrl( fname );
        final String body = getBody( fname );
        fixture.getServer()
               .expect( url, 200, body );

        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, listingFname ) );

        final HttpListing listing =
            new HttpListing( url, new ConcreteResource( location, fname ), fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( listing.getError(), nullValue() );
        assertThat( result, notNullValue() );
        assertThat( result.getListing(), notNullValue() );
        assertTrue( Arrays.equals( centralbtm, result.getListing() ) );

        final List<String> lines = IOUtils.readLines( transfer.openInputStream() );
        assertTrue( "Listing file written incorrectly!", lines.equals( Arrays.asList( centralbtm ) ) );
    }

    @Test
    public void simpleCentralListing_Missing()
        throws Exception
    {
        final String dir = "central-missing/";
        final String fname = dir + "index.html";
        final String listingFname = dir + ".listing.txt";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, listingFname ) );

        final HttpListing listing =
            new HttpListing( url, new ConcreteResource( location, fname ), fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( result, nullValue() );
        assertThat( listing.getError(), nullValue() );
    }

    @Test
    public void simpleCentralListing_Error()
        throws Exception
    {
        final String dir = "central-error/";
        final String fname = dir + "index.html";
        final String listingFname = dir + ".listing.txt";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, null );

        fixture.registerException( fixture.getUrlPath( url ), "Test Error" );

        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, listingFname ) );

        final HttpListing listing =
            new HttpListing( url, new ConcreteResource( location, fname ), fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( result, nullValue() );
        assertThat( listing.getError(), notNullValue() );
        assertTrue( listing.getError()
                           .getMessage()
                           .contains( "Test Error" ) );
    }

    @Test
    public void simpleNexusListing()
        throws Exception
    {
        final String dir = "nexus-switchyard/";
        final String fname = dir + "index.html";
        final String listingFname = dir + ".listing.txt";

        final String url = fixture.formatUrl( fname );
        final String body = getBody( fname );
        fixture.getServer()
               .expect( url, 200, body );

        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, listingFname ) );

        final HttpListing listing =
            new HttpListing( url, new ConcreteResource( location, fname ), fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( listing.getError(), nullValue() );
        assertThat( result, notNullValue() );
        assertThat( result.getListing(), notNullValue() );
        assertTrue( Arrays.equals( nexusswitchyard, result.getListing() ) );
    }

    @Test
    public void simpleNexusListing_Missing()
        throws Exception
    {
        final String dir = "nexus-missing/";
        final String fname = dir + "index.html";
        final String listingFname = dir + ".listing.txt";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, listingFname ) );

        final HttpListing listing =
            new HttpListing( url, new ConcreteResource( location, fname ), fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( listing.getError(), nullValue() );
        assertThat( result, nullValue() );
    }

    @Test
    public void simpleNexusListing_Error()
        throws Exception
    {
        final String dir = "nexus-error/";
        final String fname = dir + "index.html";
        final String listingFname = dir + ".listing.txt";

        final String url = fixture.formatUrl( fname );
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", url, true, true, true, true, null );

        fixture.registerException( fixture.getUrlPath( url ), "Test Error" );

        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, listingFname ) );

        final HttpListing listing =
            new HttpListing( url, new ConcreteResource( location, fname ), fixture.getHttp() );
        final ListingResult result = listing.call();

        assertThat( result, nullValue() );
        assertThat( listing.getError(), notNullValue() );
        assertTrue( listing.getError()
                           .getMessage()
                           .contains( "Test Error" ) );
    }
}
