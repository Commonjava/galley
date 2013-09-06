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
import org.commonjava.util.logging.Logger;

public final class HttpExistence
    implements ExistenceJob
{

    private final Logger logger = new Logger( getClass() );

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
                logger.warn( "%s : %s", line, url );
                if ( sc != HttpStatus.SC_NOT_FOUND )
                {
                    throw new TransferException( "HTTP request failed: %s", line );
                }
            }
            else
            {
                result = true;
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

        return result;
    }

    private void cleanup( final HttpHead request )
    {
        http.clearBoundCredentials( location );
        request.abort();
        http.closeConnection();
    }

}