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
package org.commonjava.maven.galley.transport.htcli.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpExchangeMetadataFromRequestHeader
        extends HttpExchangeMetadata
{
    protected Map<String, List<String>> requestHeaders = new HashMap<>();

    protected String responseStatusMessage;

    protected int responseStatusCode;

    public HttpExchangeMetadataFromRequestHeader()
    {
    }

    public HttpExchangeMetadataFromRequestHeader( final Map<String, List<String>> requestHeaders )
    {
        this.requestHeaders = requestHeaders;
    }

    public String getLastModified()
    {
        final List<String> values = requestHeaders.get( LAST_MODIFIED );
        if ( values == null || values.isEmpty() )
        {
            return null;
        }

        return values.get( 0 );
    }

    public Long getContentLength()
    {
        final List<String> values = requestHeaders.get( CONTENT_LENGTH );
        if ( values == null || values.isEmpty() )
        {
            return null;
        }

        return Long.parseLong( values.get( 0 ) );
    }
}
