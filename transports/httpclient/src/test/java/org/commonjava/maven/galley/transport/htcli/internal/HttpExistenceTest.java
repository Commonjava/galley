/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.transport.htcli.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.transport.htcli.model.SimpleHttpLocation;
import org.commonjava.maven.galley.transport.htcli.testutil.HttpTestFixture;
import org.junit.Rule;
import org.junit.Test;

public class HttpExistenceTest
{

    @Rule
    public HttpTestFixture fixture = new HttpTestFixture( "download-basic" );

    @Test
    public void simpleRetrieveOfAvailableUrl()
        throws Exception
    {
        final String fname = "simple-retrieval.html";

        final String baseUri = fixture.getBaseUri();
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, 5, null );
        final String url = fixture.formatUrl( fname );

        final HttpExistence dl = new HttpExistence( url, location, fixture.getHttp() );
        final Boolean result = dl.call();

        final TransferException error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( result, notNullValue() );

        assertThat( result, equalTo( true ) );

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
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, 5, null );
        final String url = fixture.formatUrl( fname );

        final HttpExistence dl = new HttpExistence( url, location, fixture.getHttp() );
        final Boolean result = dl.call();

        final TransferException error = dl.getError();
        assertThat( error, nullValue() );

        assertThat( result, notNullValue() );
        assertThat( result, equalTo( false ) );

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
        final SimpleHttpLocation location = new SimpleHttpLocation( "test", baseUri, true, true, true, true, 5, null );
        final String url = fixture.formatUrl( fname );

        final String error = "Test Error.";
        fixture.registerException( fixture.getUrlPath( url ), error );

        final HttpExistence dl = new HttpExistence( url, location, fixture.getHttp() );
        final Boolean result = dl.call();

        final TransferException err = dl.getError();
        assertThat( err, notNullValue() );

        assertThat( err.getMessage()
                       .endsWith( error ), equalTo( true ) );

        assertThat( result, notNullValue() );
        assertThat( result, equalTo( false ) );

        final Map<String, Integer> accessesByPath = fixture.getAccessesByPath();
        final String path = fixture.getUrlPath( url );

        assertThat( accessesByPath.get( path ), equalTo( 1 ) );
    }

}
