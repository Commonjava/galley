package org.commonjava.maven.galley.htcli;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import javax.xml.ws.Response;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.util.logging.Logger;

final class Downloader
    implements Callable<Transfer>
{

    private final Logger logger = new Logger( getClass() );

    private final String url;

    private final Location repository;

    private final Transfer target;

    private final AproxHttp http;

    private TransferException error;

    //        private final NotFoundCache nfc;

    public Downloader( /**final NotFoundCache nfc,**/
    final String url, final Repository repository, final Transfer target, final AproxHttp client )
    {
        //            this.nfc = nfc;
        this.url = url;
        this.repository = repository;
        this.target = target;
        this.http = client;
    }

    @Override
    public Transfer call()
    {
        //            logger.info( "Trying: %s", url );
        final HttpGet request = new HttpGet( url );

        http.bindRepositoryCredentialsTo( repository, request );

        try
        {
            final InputStream in = executeGet( request, url );
            writeTarget( target, in, url, repository );
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

        return target;
    }

    public TransferException getError()
    {
        return error;
    }

    private void writeTarget( final Transfer target, final InputStream in, final String url, final Repository repository )
        throws TransferException
    {
        OutputStream out = null;
        if ( in != null )
        {
            try
            {
                out = target.openOutputStream();

                copy( in, out );
            }
            catch ( final IOException e )
            {
                logger.error( "Failed to write to local proxy store: %s\nOriginal URL: %s. Reason: %s", e, target, url,
                              e.getMessage() );

                throw new TransferException( Response.serverError()
                                                     .build() );
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
                    throw new TransferException( Response.serverError()
                                                         .build() );
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
            logger.warn( "Repository remote request failed for: %s. Reason: %s", e, url, e.getMessage() );
            throw new TransferException( Response.serverError()
                                                 .build() );
        }
        catch ( final IOException e )
        {
            logger.warn( "Repository remote request failed for: %s. Reason: %s", e, url, e.getMessage() );
            throw new TransferException( Response.serverError()
                                                 .build() );
        }

        return result;
    }

    private void cleanup( final HttpGet request )
    {
        http.clearRepositoryCredentials();
        request.abort();
        http.closeConnection();
    }

}