/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.internal.util.TransferResponseUtils;
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

    private final EventMetadata eventMetadata;

    public HttpDownload( final String url, final HttpLocation location, final Transfer target,
                         final EventMetadata eventMetadata, final Http http )
    {
        this.url = url;
        this.location = location;
        this.target = target;
        this.eventMetadata = eventMetadata;
        this.http = http;
    }

    @Override
    public DownloadJob call()
    {
        final HttpGet request = new HttpGet( url );

        http.bindCredentialsTo( location, request );

        HttpResponse response = null;
        try
        {
            response = executeGet( request );
            if ( response != null )
            {
                writeTarget( request, response );
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

        logger.info( "Download attempt done: {} Result:\n  target: {}\n  error: {}", url, target, error );
        return this;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Transfer getTransfer()
    {
        return target;
    }

    private void writeTarget( final HttpGet request, final HttpResponse response )
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
                out = target.openOutputStream( TransferOperation.DOWNLOAD, true, eventMetadata );
                copy( in, out );
                logger.info( "Ensuring all HTTP data is consumed..." );
                EntityUtils.consume( entity );
                logger.info( "All HTTP data was consumed." );
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

                logger.info( "Closing output stream: {}", out );
                closeQuietly( out );
            }
        }
    }

    private HttpResponse executeGet( final HttpGet request )
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
                return TransferResponseUtils.handleUnsuccessfulResponse( request, response, url );
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
        http.closeConnection();
    }

}
