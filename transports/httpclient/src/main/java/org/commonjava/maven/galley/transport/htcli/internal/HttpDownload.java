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

    //        private final NotFoundCache nfc;

    public HttpDownload( /**final NotFoundCache nfc,**/
    final String url, final HttpLocation location, final Transfer target, final Http http )
    {
        //            this.nfc = nfc;
        this.url = url;
        this.location = location;
        this.target = target;
        this.http = http;
    }

    @Override
    public Transfer call()
    {
        //            logger.info( "Trying: %s", url );
        final HttpGet request = new HttpGet( url );

        http.bindCredentialsTo( location, request );

        try
        {
            final InputStream in = executeGet( request, url );
            writeTarget( target, in, url, location );
        }
        catch ( final TransferException e )
        {
            //                try
            //                {
            //                    nfc.addMissing( url );
            //                }
            //                catch ( final ProxyDataException e1 )
            //                {
            //                    logger.error( "Failed to add NFC entry for: %s. Reason: %s", e1, url, e1.getMessage() );
            //                }

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

    private void writeTarget( final Transfer target, final InputStream in, final String url, final Location repository )
        throws TransferException
    {
        OutputStream out = null;
        if ( in != null )
        {
            try
            {
                out = target.openOutputStream( TransferOperation.DOWNLOAD );

                copy( in, out );
            }
            catch ( final IOException e )
            {
                throw new TransferException( "Failed to write to local proxy store: %s\nOriginal URL: %s. Reason: %s",
                                             e, target, url, e.getMessage() );
            }
            finally
            {
                closeQuietly( in );
                closeQuietly( out );
            }
        }
    }

    private InputStream executeGet( final HttpGet request, final String url )
        throws TransferException
    {
        InputStream result = null;

        try
        {
            final HttpResponse response = http.getClient()
                                              .execute( request );
            final StatusLine line = response.getStatusLine();
            final int sc = line.getStatusCode();
            if ( sc != HttpStatus.SC_OK )
            {
                logger.warn( "%s : %s", line, url );
                if ( sc == HttpStatus.SC_NOT_FOUND )
                {
                    result = null;
                }
                else
                {
                    throw new TransferException( "HTTP request failed: %s", line );
                }
            }
            else
            {
                result = response.getEntity()
                                 .getContent();
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

    private void cleanup( final HttpGet request )
    {
        http.clearBoundCredentials( location );
        request.abort();
        http.closeConnection();
    }

}