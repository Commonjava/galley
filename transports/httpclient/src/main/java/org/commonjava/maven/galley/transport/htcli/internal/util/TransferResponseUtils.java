package org.commonjava.maven.galley.transport.htcli.internal.util;

import static org.apache.commons.io.IOUtils.copy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.AbstractExecutionAwareRequest;
import org.apache.http.util.EntityUtils;
import org.commonjava.maven.galley.TransferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransferResponseUtils
{

    private TransferResponseUtils()
    {
    }

    public static HttpResponse handleUnsuccessfulResponse( final AbstractExecutionAwareRequest request,
                                                           final HttpResponse response, final String url )
        throws TransferException
    {
        return handleUnsuccessfulResponse( request, response, url, true );
    }

    public static HttpResponse handleUnsuccessfulResponse( final AbstractExecutionAwareRequest request,
                                                           final HttpResponse response, final String url,
                                                           final boolean graceful404 )
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
            if ( graceful404 && sc == HttpStatus.SC_NOT_FOUND )
            {
                return null;
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

                throw new TransferException( "HTTP request failed: %s%s", line, ( out == null ? "" : "\n\n"
                    + new String( out.toByteArray() ) ) );
            }
        }
        catch ( final IOException e )
        {
            request.abort();
            throw new TransferException(
                                         "Error reading body of unsuccessful request.\nStatus: %s.\nURL: %s.\nReason: %s",
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
