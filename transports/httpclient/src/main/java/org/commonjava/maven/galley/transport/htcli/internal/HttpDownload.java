package org.commonjava.maven.galley.transport.htcli.internal;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
import org.commonjava.util.logging.Logger;

public final class HttpDownload
    implements DownloadJob
{

    private final Logger logger = new Logger( getClass() );

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

        try
        {
            final HttpResponse response = executeGet( request, url );
            writeTarget( target, request, response, url, location );
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

    private void writeTarget( final Transfer target, final HttpGet request, final HttpResponse response, final String url, final Location repository )
        throws TransferException
    {
        OutputStream out = null;
        if ( response != null )
        {
            InputStream in = null;
            try
            {
                in = response.getEntity()
                             .getContent();
                out = target.openOutputStream( TransferOperation.DOWNLOAD, false );

                copy( in, out );
            }
            catch ( final IOException e )
            {
                request.abort();
                throw new TransferException( "Failed to write to local proxy store: %s\nOriginal URL: %s. Reason: %s", e, target, url, e.getMessage() );
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
            if ( sc != HttpStatus.SC_OK )
            {
                EntityUtils.consume( response.getEntity() );

                logger.warn( "%s : %s", line, url );
                if ( sc == HttpStatus.SC_NOT_FOUND )
                {
                    return null;
                }
                else
                {
                    throw new TransferException( "HTTP request failed: %s", line );
                }
            }
            else
            {
                return response;
            }
        }
        catch ( final ClientProtocolException e )
        {
            throw new TransferException( "Repository remote request failed for: %s. Reason: %s", e, url, e.getMessage() );
        }
        catch ( final IOException e )
        {
            throw new TransferException( "Repository remote request failed for: %s. Reason: %s", e, url, e.getMessage() );
        }
    }

    private void cleanup( final HttpGet request )
    {
        http.clearBoundCredentials( location );
        //        http.closeConnection();
    }

}