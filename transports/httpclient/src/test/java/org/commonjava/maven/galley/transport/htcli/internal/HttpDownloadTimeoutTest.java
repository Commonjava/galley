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

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.config.TransportMetricConfig;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.transport.htcli.model.SimpleHttpLocation;
import org.commonjava.maven.galley.transport.htcli.testutil.HttpTestFixture;
import org.commonjava.test.http.expect.ExpectationHandler;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.commonjava.maven.galley.transport.htcli.testutil.TestMetricConfig.disabledMetricConfig;
import static org.commonjava.maven.galley.transport.htcli.testutil.TestMetricConfig.metricRegistry;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith( BMUnitRunner.class )
@BMUnitConfig( debug = true )
public class HttpDownloadTimeoutTest
{
    final Logger logger = LoggerFactory.getLogger( getClass() );

    @Rule
    public HttpTestFixture fixture = new HttpTestFixture( "download-basic" );

    @Test
    public void downloadTransferTimeout()
            throws Exception
    {
        final String content = "1234567890";
        final String path = "/path/to/file";

        fixture.getServer().expect( "GET", fixture.formatUrl( path ), new ExpectationHandler()
        {
            @Override
            public void handle( final HttpServletRequest httpServletRequest,
                                final HttpServletResponse httpServletResponse )
                    throws ServletException, IOException
            {
                httpServletResponse.setStatus( 200 );
                httpServletResponse.setHeader( "Content-Length", Integer.toString( content.length() ) );
                OutputStream output = httpServletResponse.getOutputStream();
                for ( byte aByte : content.getBytes() )
                {
                    try
                    {
                        sleep(1000);
                    }
                    catch ( InterruptedException e )
                    {
                        ;
                    }
                    logger.debug( ">>> write 1 byte" );
                    output.write( aByte );
                }
            }
        } );

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, path ) );
        final String url = fixture.formatUrl( path );

        assertThat( transfer.exists(), equalTo( false ) );

        HttpDownload dl = new HttpDownload( url, location, transfer, new HashMap<Transfer, Long>(), new EventMetadata(),
                               fixture.getHttp(), new ObjectMapper(), metricRegistry, disabledMetricConfig );


        logger.debug( "**Download started**" );
        DownloadJob resultJob = dl.call();
        logger.debug( "**Download completed**" );

        TransferException error = dl.getError();
        assertThat( error, nullValue() );
        assertThat( resultJob, notNullValue() );

        Transfer result = resultJob.getTransfer();

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( true ) );
        assertThat( transfer.exists(), equalTo( true ) );

    }
}
