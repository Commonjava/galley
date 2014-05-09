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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpDownload
    implements DownloadJob
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final String url;

    private final HttpLocation location;

    private final Transfer target;

    private final Http http;

    private TransferException error;

    public HttpDownload( final String url, final HttpLocation location, final Transfer target, final Http http )
    {
        this.url = url;
        this.location = location;
        this.target = target;
        this.http = http;
    }

    @Override
    public Transfer call()
    {
        final HttpGet request = new HttpGet( url );

        http.bindCredentialsTo( location, request );

        HttpResponse response = null;
        try
        {
            response = executeGet( request, url );
            if ( response != null )
            {
                writeTarget( target, request, response, url, location );
            }
        }
        catch ( final TransferException e )
        {
            this.error = e;
        }
        finally
        {
            cleanup( request );
        }

        return error == null ? target : null;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    private void writeTarget( final Transfer target, final HttpGet request, final HttpResponse response,
                              final String url, final Location repository )
        throws TransferException
    {
        OutputStream out = null;
        if ( response != null )
        {
            InputStream in = null;
            try
            {
                final HttpEntity entity = response.getEntity();

                in = entity.getContent();
                out = target.openOutputStream( TransferOperation.DOWNLOAD, false );
                copy( in, out );
                EntityUtils.consume( entity );
            }
            catch ( final IOException e )
            {
                request.abort();
                throw new TransferException( "Failed to write to local proxy store: {}\nOriginal URL: {}. Reason: {}",
                                             e, target, url, e.getMessage() );
            }
            finally
            {
                closeQuietly( in );
                closeQuietly( out );
            }
        }
    }

    private HttpResponse executeGet( final HttpGet request, final String url )
        throws TransferException
    {
        try
        {
            final HttpResponse response = http.getClient()
                                              .execute( request );
            final StatusLine line = response.getStatusLine();
            final int sc = line.getStatusCode();
            logger.debug( "GET {} : {}", line, url );
            if ( sc != HttpStatus.SC_OK )
            {
                EntityUtils.consume( response.getEntity() );

                if ( sc == HttpStatus.SC_NOT_FOUND )
                {
                    return null;
                }
                else
                {
                    throw new TransferException( "HTTP request failed: {}", line );
                }
            }
            else
            {
                return response;
            }
        }
        catch ( final ClientProtocolException e )
        {
            request.abort();
            throw new TransferException( "Repository remote request failed for: {}. Reason: {}", e, url, e.getMessage() );
        }
        catch ( final IOException e )
        {
            request.abort();
            throw new TransferException( "Repository remote request failed for: {}. Reason: {}", e, url, e.getMessage() );
        }
    }

    private void cleanup( final HttpGet request )
    {
        http.clearBoundCredentials( location );
        //        http.closeConnection();
    }

}
