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
package org.commonjava.maven.galley.transport.htcli.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.commonjava.maven.galley.io.SpecialPathConstants;

public class HttpExchangeMetadata
{

    public static final String FILE_EXTENSION = SpecialPathConstants.HTTP_METADATA_EXT;

    protected static final String LAST_MODIFIED = "LAST-MODIFIED";

    protected static final Object CONTENT_LENGTH = "CONTENT-LENGTH";

    protected Map<String, List<String>> requestHeaders = new HashMap<>();

    protected Map<String, List<String>> responseHeaders = new HashMap<>();

    protected String responseStatusMessage;

    protected int responseStatusCode;

    public HttpExchangeMetadata()
    {
    }

    public HttpExchangeMetadata( final HttpRequest request, final HttpResponse response )
    {
        populateHeaders( requestHeaders, request.getAllHeaders() );
        populateHeaders( responseHeaders, response.getAllHeaders() );

        final StatusLine sl = response.getStatusLine();
        this.responseStatusCode = sl.getStatusCode();
        this.responseStatusMessage = sl.getReasonPhrase();
    }

    private void populateHeaders( final Map<String, List<String>> headerMap, final Header[] allHeaders )
    {
        for ( final Header header : allHeaders )
        {
            List<String> values = headerMap.get( header.getName() );
            if ( values == null )
            {
                values = new ArrayList<>();
                headerMap.put( header.getName()
                                     .toUpperCase(), values );
            }

            values.add( header.getValue() );
        }
    }

    public Map<String, List<String>> getRequestHeaders()
    {
        return requestHeaders;
    }

    public void setRequestHeaders( final Map<String, List<String>> requestHeaders )
    {
        this.requestHeaders = requestHeaders;
    }

    public Map<String, List<String>> getResponseHeaders()
    {
        return responseHeaders;
    }

    public void setResponseHeaders( final Map<String, List<String>> responseHeaders )
    {
        this.responseHeaders = responseHeaders;
    }

    public String getResponseStatusMessage()
    {
        return responseStatusMessage;
    }

    public void setResponseStatusMessage( final String responseStatusMessage )
    {
        this.responseStatusMessage = responseStatusMessage;
    }

    public int getResponseStatusCode()
    {
        return responseStatusCode;
    }

    public void setResponseStatusCode( final int responseStatusCode )
    {
        this.responseStatusCode = responseStatusCode;
    }

    public String getLastModified()
    {
        final List<String> values = responseHeaders.get( LAST_MODIFIED );
        if ( values == null || values.isEmpty() )
        {
            return null;
        }

        return values.get( 0 );
    }

    public Long getContentLength()
    {
        final List<String> values = responseHeaders.get( CONTENT_LENGTH );
        if ( values == null || values.isEmpty() )
        {
            return null;
        }

        return Long.parseLong( values.get( 0 ) );
    }

}
