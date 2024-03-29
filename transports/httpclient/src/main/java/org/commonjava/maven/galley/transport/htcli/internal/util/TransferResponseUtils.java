/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
package org.commonjava.maven.galley.transport.htcli.internal.util;

import static org.apache.commons.io.IOUtils.copy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.commonjava.maven.galley.BadGatewayException;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferLocationException;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransferResponseUtils
{

    private static final Set<Integer> NON_SERVER_GATEWAY_ERRORS =
        Collections.unmodifiableSet( new HashSet<>( Collections.singletonList( 410 ) ) );

    private TransferResponseUtils()
    {
    }

    public static boolean handleUnsuccessfulResponse( final HttpUriRequest request, final CloseableHttpResponse response,
                                                      HttpLocation location, final String url )
        throws TransferException
    {
        return handleUnsuccessfulResponse( request, response, location, url, true );
    }

    public static boolean handleUnsuccessfulResponse( final HttpUriRequest request, final CloseableHttpResponse response,
                                                      HttpLocation location, final String url, final boolean graceful404 )
        throws TransferException
    {
        final Logger logger = LoggerFactory.getLogger( TransferResponseUtils.class );

        final StatusLine line = response.getStatusLine();
        InputStream in = null;
        HttpEntity entity = null;
        try
        {
            entity = response.getEntity();
            final int sc = line.getStatusCode();
            boolean contentMissing = ( sc == HttpStatus.SC_NOT_FOUND || sc == HttpStatus.SC_GONE );

            if ( graceful404 && contentMissing )
            {
                return false;
            }
            else
            {
                ByteArrayOutputStream out = null;
                if ( entity != null )
                {
                    in = entity.getContent();
                    out = new ByteArrayOutputStream();
                    copy( in, out );
                }

                if ( NON_SERVER_GATEWAY_ERRORS.contains( sc ) || ( sc > 499 && sc < 599 ) )
                {
                    throw new BadGatewayException( location, url, sc, "HTTP request failed: %s%s", line,
                                                   ( out == null ? "" : "\n\n" + out ) );
                }
                else if ( contentMissing )
                {
                    throw new TransferException( "HTTP request failed: %s\nURL: %s%s", line, url, ( out == null ?
                            "" :
                            "\n\n" + out ) );
                }
                else
                {
                    throw new TransferLocationException( location, "HTTP request failed: %s%s", line, ( out == null ?
                            "" :
                            "\n\n" + out ) );
                }
            }
        }
        catch ( final IOException e )
        {
            request.abort();
            throw new TransferLocationException(
                                         location, "Error reading body of unsuccessful request.\nStatus: %s.\nURL: %s.\nReason: %s",
                                         e, line, url, e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( in );
            if ( entity != null )
            {
                try
                {
                    EntityUtils.consume( entity );
                }
                catch ( final IOException e )
                {
                    logger.debug( "Failed to consume entity: " + e.getMessage(), e );
                }
            }
        }
    }

}
