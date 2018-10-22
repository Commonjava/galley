/**
 * Copyright (C) 2013 Red Hat, Inc.
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
package org.commonjava.maven.galley.transport.htcli;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.maven.galley.config.TransportMetricConfig;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.OverriddenBooleanValue;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.transport.htcli.internal.HttpDownload;
import org.commonjava.maven.galley.transport.htcli.model.SimpleHttpLocation;
import org.commonjava.maven.galley.transport.htcli.testutil.HttpTestFixture;
import org.commonjava.test.http.expect.ExpectationHandler;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ContentFilteringTransferDecoratorTest
{
    @Rule
    public HttpTestFixture fixture = new HttpTestFixture( "test" );

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

    @Test
    public void snapshotNotExistsWhenSnapshotsNotAllowed()
            throws Exception
    {
        final String fname = "/commons-codec/commons-codec/11-SNAPSHOT/maven-metadata.xml.md5";

        final String content = "kljsjdlfkjsdlkj123j13=20=s0dfjklxjkj";
        fixture.getServer().expect( "GET", fixture.formatUrl( fname ), new ExpectationHandler()
        {

            @Override
            public void handle( final HttpServletRequest httpServletRequest,
                                final HttpServletResponse httpServletResponse )
                    throws ServletException, IOException
            {
                httpServletResponse.setStatus( 200 );
                httpServletResponse.setHeader( "Content-Length", Integer.toString( content.length() ) );
                PrintWriter writer = httpServletResponse.getWriter();

                writer.write( content );
            }
        } );

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, false, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, fname ) );
        final String url = fixture.formatUrl( fname );

        assertThat( transfer.exists(), equalTo( false ) );

        HttpDownload dl = new HttpDownload( url, location, transfer, new HashMap<Transfer, Long>(), new EventMetadata(),
                                            fixture.getHttp(), new ObjectMapper(), metricRegistry, metricConfig );

        DownloadJob resultJob = dl.call();

        assertThat( resultJob, notNullValue() );

        Transfer result = resultJob.getTransfer();

        ContentsFilteringTransferDecorator decorator = new ContentsFilteringTransferDecorator();
        OverriddenBooleanValue value = decorator.decorateExists( result, new EventMetadata() );

        assertThat( value.overrides(), equalTo( true ) );
        assertThat( value.getResult(), equalTo( false ) );
    }

    @Test
    public void snapshotListingNotInWhenSnapshotsNotAllowedWithNoVersionPath()
            throws Exception
    {
        final String fname = "commons-codec/commons-codec/";
        final SimpleHttpLocation location =
                new SimpleHttpLocation( "test", "http://test", false, true, false, false, null );
        final ConcreteResource resource = new ConcreteResource( location, fname );
        final Transfer transfer = new Transfer( resource, null, null, null );

        String[] listing = Arrays.asList( "1.0/", "1.0-SNAPSHOT/", "1.1/", "1.1-SNAPSHOT/" ).toArray( new String[4] );
        ContentsFilteringTransferDecorator decorator = new ContentsFilteringTransferDecorator();
        listing = decorator.decorateListing( transfer, listing, new EventMetadata() );

        System.out.println( Arrays.asList( listing ) );

        assertThat( listing, CoreMatchers.<String[]>notNullValue() );
        assertThat( listing.length, equalTo( 2 ) );
        assertThat( Arrays.asList( listing ).contains( "1.0-SNAPSHOT/" ), equalTo( false ) );
        assertThat( Arrays.asList( listing ).contains( "1.1-SNAPSHOT/" ), equalTo( false ) );
    }

    @Test
    public void snapshotListingNotInWhenSnapshotsNotAllowedWithVersionPath()
            throws Exception
    {
        final String fname = "commons-codec/commons-codec/1.1-SNAPSHOT/";
        final SimpleHttpLocation location =
                new SimpleHttpLocation( "test", "http://test", false, true, false, false, null );
        final ConcreteResource resource = new ConcreteResource( location, fname );
        final Transfer transfer = new Transfer( resource, null, null, null );

        String[] listing = Arrays.asList( "commons-codec-1.1-SNAPSHOT.jar", "commons-codec-1.1-SNAPSHOT-source.jar",
                                          "maven-metadata.xml" ).toArray( new String[4] );
        ContentsFilteringTransferDecorator decorator = new ContentsFilteringTransferDecorator();
        listing = decorator.decorateListing( transfer, listing, new EventMetadata() );

        assertThat( listing, CoreMatchers.<String[]>notNullValue() );
        assertThat( listing.length, equalTo( 0 ) );
    }

    @Test
    public void releaseListingInWhenSnapshotsNotAllowedWithVersionPath()
            throws Exception
    {
        final String fname = "commons-codec/commons-codec/1.1/";
        final SimpleHttpLocation location =
                new SimpleHttpLocation( "test", "http://test", false, true, false, false, null );
        final ConcreteResource resource = new ConcreteResource( location, fname );
        final Transfer transfer = new Transfer( resource, null, null, null );
        final List<String> listElems =
                Arrays.asList( "commons-codec-1.1.jar", "commons-codec-1.1-source.jar", "maven-metadata.xml" );

        String[] listing = listElems.toArray( new String[3] );
        ContentsFilteringTransferDecorator decorator = new ContentsFilteringTransferDecorator();
        listing = decorator.decorateListing( transfer, listing, new EventMetadata() );

        assertThat( listing, CoreMatchers.<String[]>notNullValue() );
        assertThat( listing.length, equalTo( 3 ) );
        assertThat( Arrays.asList( listing ).containsAll(listElems), equalTo( true ) );
    }

}
