/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.transport.htcli.internal;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpExistence
    implements ExistenceJob
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final String url;

    private final HttpLocation location;

    private final Http http;

    private TransferException error;

    public HttpExistence( final String url, final HttpLocation location, final Http http )
    {
        this.url = url;
        this.location = location;
        this.http = http;
    }

    @Override
    public Boolean call()
    {
        final HttpHead request = new HttpHead( url );

        http.bindCredentialsTo( location, request );

        try
        {
            return execute( request, url );
        }
        catch ( final TransferException e )
        {
            this.error = e;
        }
        finally
        {
            cleanup( request );
        }

        return false;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    private boolean execute( final HttpHead request, final String url )
        throws TransferException
    {
        boolean result = false;

        try
        {
            final HttpResponse response = http.getClient()
                                              .execute( request );
            final StatusLine line = response.getStatusLine();
            final int sc = line.getStatusCode();
            if ( sc != HttpStatus.SC_OK )
            {
                logger.debug( "{} : {}", line, url );
                if ( sc != HttpStatus.SC_NOT_FOUND )
                {
                    throw new TransferException( "HTTP request failed: {}", line );
                }
            }
            else
            {
                result = true;
            }
        }
        catch ( final ClientProtocolException e )
        {
            throw new TransferException( "Repository remote request failed for: {}. Reason: {}", e, url, e.getMessage() );
        }
        catch ( final IOException e )
        {
            throw new TransferException( "Repository remote request failed for: {}. Reason: {}", e, url, e.getMessage() );
        }

        return result;
    }

    private void cleanup( final HttpHead request )
    {
        http.clearBoundCredentials( location );
        request.abort();
        http.closeConnection();
    }

}
