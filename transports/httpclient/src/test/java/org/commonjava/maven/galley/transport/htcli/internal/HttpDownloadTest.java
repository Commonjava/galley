/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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

import java.util.Map;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.transport.htcli.model.SimpleHttpLocation;
import org.commonjava.maven.galley.transport.htcli.testutil.HttpTestFixture;
import org.junit.Rule;
import org.junit.Test;

public class HttpDownloadTest
{

    @Rule
    public HttpTestFixture fixture = new HttpTestFixture( "download-basic" );

    @Test
    public void simpleRetrieveOfAvailableUrl()
        throws Exception
    {
        final String fname = "simple-retrieval.html";

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, null );
        final Transfer transfer = fixture.getTransfer( new ConcreteResource( location, fname ) );
        final String url = fixture.formatUrl( fname );

        assertThat( transfer.exists(), equalTo( false ) );

        final HttpDownload dl = new HttpDownload( url, location, transfer, fixture.getHttp() );
        final Transfer result = dl.call();

        final TransferException error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( result, notNullValue() );

        assertThat( result.exists(), equalTo( true ) );
        assertThat( transfer.exists(), equalTo( true ) );

        final Map<String, Integer> accessesByPath = fixture.getAccessesByPath();
        final String path = fixture.getUrlPath( url );

        assertThat( accessesByPath.get( path ), equalTo( 1 ) );
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

        assertThat( transfer.exists(), equalTo( false ) );

        final HttpDownload dl = new HttpDownload( url, location, transfer, fixture.getHttp() );
        final Transfer result = dl.call();

        final TransferException error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( result, notNullValue() );
        assertThat( result.exists(), equalTo( false ) );
        assertThat( transfer.exists(), equalTo( false ) );

        final Map<String, Integer> accessesByPath = fixture.getAccessesByPath();
        final String path = fixture.getUrlPath( url );

        assertThat( accessesByPath.get( path ), equalTo( 1 ) );
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
        fixture.registerException( fixture.getUrlPath( url ), error );

        assertThat( transfer.exists(), equalTo( false ) );

        final HttpDownload dl = new HttpDownload( url, location, transfer, fixture.getHttp() );
        final Transfer result = dl.call();

        final TransferException err = dl.getError();
        assertThat( err, notNullValue() );
        assertThat( err.getMessage()
                       .endsWith( error ), equalTo( true ) );

        assertThat( result, nullValue() );
        assertThat( transfer.exists(), equalTo( false ) );

        final Map<String, Integer> accessesByPath = fixture.getAccessesByPath();
        final String path = fixture.getUrlPath( url );

        assertThat( accessesByPath.get( path ), equalTo( 1 ) );
    }

}
