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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.config.TransportMetricConfig;
import org.commonjava.maven.galley.transport.htcli.model.SimpleHttpLocation;
import org.commonjava.maven.galley.transport.htcli.testutil.HttpTestFixture;
import org.commonjava.test.http.expect.ExpectationHandler;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith( BMUnitRunner.class )
@BMUnitConfig( debug = true )
public class HttpDownloadTest
{
    private static MetricRegistry metricRegistry = new MetricRegistry();

    private static TransportMetricConfig metricConfig = new TransportMetricConfig()
    {
        @Override
        public boolean isEnabled()
        {
            return true;
        }

        @Override
        public String getNodePrefix()
        {
            return null;
        }

        @Override
        public String getMetricUniqueName( Location location )
        {
            if ( location.getName().equals( "test" ) )
            {
                return location.getName();
            }
            return null;
        }
    };

    @Rule
    public HttpTestFixture fixture = new HttpTestFixture( "download-basic" );

    @Test
    @BMRule( name="throw IOException during writeTarget copy operation",
             targetClass = "HttpDownload",
             targetMethod = "doCopy",
             targetLocation = "ENTRY",
             condition = "!flagged(\"firstCopy\")",
             action="flag(\"firstCopy\"); throw new IOException(\"BMUnit exception\");" )
    public void IOExceptionDuringDownloadTransferDeletesTargetFile()
            throws Exception
    {
        final String content = "This is some content " + System.currentTimeMillis() + "." + System.nanoTime();
        final String path = "/path/to/file";

        fixture.getServer().expect( "GET", fixture.formatUrl( path ), new ExpectationHandler()
        {
            int count=0;

            @Override
            public void handle( final HttpServletRequest httpServletRequest,
                                final HttpServletResponse httpServletResponse )
                    throws ServletException, IOException
            {
                httpServletResponse.setStatus( 200 );
                httpServletResponse.setHeader( "Content-Length", Integer.toString( content.length() ) );
                PrintWriter writer = httpServletResponse.getWriter();

                if ( count < 1 )
                {
                    writer.write( content.substring( 0, content.length() / 2 ) );
                }
                else
                {
                    writer.write( content );
                }

                count++;
            }
        } );

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, path ) );
        final String url = fixture.formatUrl( path );

        assertThat( transfer.exists(), equalTo( false ) );

        // first call, server should quit transferring halfway through the transfer

        HttpDownload dl =
                new HttpDownload( url, location, transfer, new HashMap<Transfer, Long>(), new EventMetadata(),
                                  fixture.getHttp(), new ObjectMapper(), metricRegistry, metricConfig );

        DownloadJob resultJob = dl.call();

        TransferException error = dl.getError();
        assertThat( error, notNullValue() );
        error.printStackTrace();

        assertThat( resultJob, notNullValue() );

        Transfer result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( false ) );
        assertThat( transfer.exists(), equalTo( false ) );

        // second call should hit upstream again and succeed.

        dl = new HttpDownload( url, location, transfer, new HashMap<Transfer, Long>(), new EventMetadata(),
                               fixture.getHttp(), new ObjectMapper(), metricRegistry, metricConfig );

        resultJob = dl.call();

        error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( resultJob, notNullValue() );

        result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( true ) );
        assertThat( transfer.exists(), equalTo( true ) );

        final String urlPath = fixture.getUrlPath( url );

        assertThat( fixture.getAccessesFor( urlPath ), equalTo( 2 ) );
    }

    @Test
    public void partialDownloadDeletesTargetFile()
            throws Exception
    {
        final String content = "This is some content " + System.currentTimeMillis() + "." + System.nanoTime();
        final String path = "/path/to/file";

        fixture.getServer().expect( "GET", fixture.formatUrl( path ), new ExpectationHandler()
        {
            int count=0;

            @Override
            public void handle( final HttpServletRequest httpServletRequest,
                                final HttpServletResponse httpServletResponse )
                    throws ServletException, IOException
            {
                httpServletResponse.setStatus( 200 );
                httpServletResponse.setHeader( "Content-Length", Integer.toString( content.length() ) );
                PrintWriter writer = httpServletResponse.getWriter();

                if ( count < 1 )
                {
                    writer.write( content.substring( 0, content.length() / 2 ) );
                }
                else
                {
                    writer.write( content );
                }

                count++;
            }
        } );

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, path ) );
        final String url = fixture.formatUrl( path );

        assertThat( transfer.exists(), equalTo( false ) );

        // first call, server should quit transferring halfway through the transfer

        HttpDownload dl =
                new HttpDownload( url, location, transfer, new HashMap<Transfer, Long>(), new EventMetadata(),
                                  fixture.getHttp(), new ObjectMapper(), metricRegistry, metricConfig );

        DownloadJob resultJob = dl.call();

        TransferException error = dl.getError();
        assertThat( error, notNullValue() );
        error.printStackTrace();

        assertThat( resultJob, notNullValue() );

        Transfer result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( false ) );
        assertThat( transfer.exists(), equalTo( false ) );

        // second call should hit upstream again and succeed.

        dl = new HttpDownload( url, location, transfer, new HashMap<Transfer, Long>(), new EventMetadata(),
                               fixture.getHttp(), new ObjectMapper(), metricRegistry, metricConfig );

        resultJob = dl.call();

        error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( resultJob, notNullValue() );

        result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( true ) );
        assertThat( transfer.exists(), equalTo( true ) );

        final String urlPath = fixture.getUrlPath( url );

        assertThat( fixture.getAccessesFor( urlPath ), equalTo( 2 ) );
    }

    @Test
    public void simpleRetrieveOfAvailableUrl()
        throws Exception
    {
        final String fname = "simple-retrieval.html";

        fixture.getServer()
               .expect( fixture.formatUrl( fname ), 200, fname );

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, fname ) );
        final String url = fixture.formatUrl( fname );

        Map<Transfer, Long> transferSizes = new HashMap<Transfer, Long>();

        assertThat( transfer.exists(), equalTo( false ) );

        final HttpDownload dl =
            new HttpDownload( url, location, transfer, transferSizes, new EventMetadata(), fixture.getHttp(), new ObjectMapper(),
                              metricRegistry, metricConfig );
        final DownloadJob resultJob = dl.call();

        final TransferException error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( resultJob, notNullValue() );

        final Transfer result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( true ) );
        assertThat( transfer.exists(), equalTo( true ) );

        final String path = fixture.getUrlPath( url );

        assertThat( fixture.getAccessesFor( path ), equalTo( 1 ) );
    }

    @Test
    public void simpleRetriveOfRedirectUrl() throws Exception {
        final String content = "This is some content " + System.currentTimeMillis() + "." + System.nanoTime();
        final String redirectPath = "/path/to/file";
        final String path = "/redirect/to/file";

        fixture.getServer().expect( "GET", fixture.formatUrl( path ), new ExpectationHandler()
        {
            @Override
            public void handle( final HttpServletRequest httpServletRequest,
                                final HttpServletResponse httpServletResponse )
                    throws ServletException, IOException
            {
                httpServletResponse.setStatus( 302 );
                httpServletResponse.setHeader( "Location", fixture.formatUrl( redirectPath ) );
            }
        } );

        fixture.getServer().expect( "GET", fixture.formatUrl( redirectPath ), 200, content );

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, path ) );
        final String url = fixture.formatUrl( path );

        Map<Transfer, Long> transferSizes = new HashMap<Transfer, Long>();

        assertThat( transfer.exists(), equalTo( false ) );

        final HttpDownload dl =
                new HttpDownload( url, location, transfer, transferSizes, new EventMetadata(), fixture.getHttp(), new ObjectMapper(),
                                  metricRegistry, metricConfig );
        final DownloadJob resultJob = dl.call();

        final TransferException error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( resultJob, notNullValue() );

        final Transfer result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( true ) );
        assertThat( transfer.exists(), equalTo( true ) );

        final String postPath = fixture.getUrlPath( url );

        assertThat( fixture.getAccessesFor( postPath ), equalTo( 1 ) );
    }

    @Test
    public void simpleRetrieveOfMissingUrl()
        throws Exception
    {
        final String fname = "simple-missing.html";

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, fname ) );
        final String url = fixture.formatUrl( fname );

        Map<Transfer, Long> transferSizes = new HashMap<Transfer, Long>();

        assertThat( transfer.exists(), equalTo( false ) );

        final HttpDownload dl =
            new HttpDownload( url, location, transfer, transferSizes, new EventMetadata(), fixture.getHttp(), new ObjectMapper(),
                              metricRegistry, metricConfig );
        final DownloadJob resultJob = dl.call();

        final TransferException error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( resultJob, notNullValue() );

        final Transfer result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( false ) );
        assertThat( transfer.exists(), equalTo( false ) );

        final String path = fixture.getUrlPath( url );

        assertThat( fixture.getAccessesFor( path ), equalTo( 1 ) );
    }

    @Test
    public void simpleRetrieveOfUrlWithError()
        throws Exception
    {
        final String fname = "simple-error.html";

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, fname ) );
        final String url = fixture.formatUrl( fname );

        final String error = "Test Error.";
        final String path = fixture.getUrlPath( url );
        fixture.registerException( path, error );

        Map<Transfer, Long> transferSizes = new HashMap<Transfer, Long>();

        assertThat( transfer.exists(), equalTo( false ) );

        final HttpDownload dl =
            new HttpDownload( url, location, transfer, transferSizes, new EventMetadata(), fixture.getHttp(), new ObjectMapper(),
                              metricRegistry, metricConfig );
        final DownloadJob resultJob = dl.call();

        final TransferException err = dl.getError();
        assertThat( err, notNullValue() );

        assertThat( err.getMessage()
                       .contains( error ), equalTo( true ) );

        assertThat( resultJob, notNullValue() );

        final Transfer result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( transfer.exists(), equalTo( false ) );


        assertThat( fixture.getAccessesFor( path ), equalTo( 1 ) );
    }

    @Test
    public void simpleRetrieveOfAvailableUrl_MetricTest() throws Exception
    {
        startReport();
        simpleRetrieveOfAvailableUrl();
        waitSeconds( 3 ); // wait for a while to see the metric
    }

    private void startReport()
    {
        ConsoleReporter reporter = ConsoleReporter.forRegistry( metricRegistry )
                                                  .convertRatesTo( TimeUnit.SECONDS )
                                                  .convertDurationsTo( TimeUnit.MILLISECONDS )
                                                  .build();
        reporter.start( 1, TimeUnit.SECONDS );
    }

    private void waitSeconds( int seconds )
    {
        try
        {
            Thread.sleep( seconds * 1000 );
        }
        catch ( InterruptedException e )
        {
        }
    }
}
