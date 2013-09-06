package org.commonjava.maven.galley.internal.xfer;

import static org.commonjava.maven.galley.util.UrlUtils.buildUrl;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.util.logging.Logger;

@ApplicationScoped
public class DownloadHandler
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private NotFoundCache nfc;

    private final Map<String, Future<Transfer>> pending = new ConcurrentHashMap<>();

    @Inject
    @ExecutorConfig( threads = 12, daemon = true, named = "galley-transfers", priority = 8 )
    private ExecutorService executor;

    public DownloadHandler()
    {
    }

    public DownloadHandler( final NotFoundCache nfc, final ExecutorService executor )
    {
        this.nfc = nfc;
        this.executor = executor;
    }

    public Transfer download( final Resource resource, final Transfer target, final int timeoutSeconds, final Transport transport,
                              final boolean suppressFailures )
        throws TransferException
    {
        if ( !resource.allowsDownloading() )
        {
            return null;
        }

        String url;
        try
        {
            url = buildUrl( resource );
        }
        catch ( final MalformedURLException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed to build URL for resource: %s. Reason: %s", e, resource, e.getMessage() );
            }
            else
            {
                return null;
            }
        }

        if ( url == null )
        {
            return null;
        }

        if ( nfc.isMissing( resource ) )
        {
            logger.info( "NFC: Already marked as missing: %s", resource );
            return null;
        }

        logger.info( "RETRIEVE %s", url );

        //        if ( nfc.hasEntry( url ) )
        //        {
        //            fileEventManager.fire( new FileNotFoundEvent( repository, target.getPath() ) );
        //            return false;
        //        }

        Transfer result = joinDownload( url, target, timeoutSeconds, suppressFailures );
        if ( result == null )
        {
            result = startDownload( url, resource, target, timeoutSeconds, transport, suppressFailures );
        }

        return result;
    }

    private Transfer joinDownload( final String url, final Transfer target, final int timeoutSeconds, final boolean suppressFailures )
        throws TransferException
    {
        // if the target file already exists, skip joining.
        if ( target.exists() )
        {
            return target;
        }
        else
        {
            final Future<Transfer> future = pending.get( url );
            if ( future != null )
            {
                Transfer f = null;
                try
                {
                    f = future.get( timeoutSeconds, TimeUnit.SECONDS );

                    return f;
                }
                catch ( final InterruptedException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Download interrupted: %s", e, url );
                    }
                }
                catch ( final ExecutionException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Download failed: %s", e, url );
                    }
                }
                catch ( final TimeoutException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Timeout on: %s", e, url );
                    }
                }
            }
        }

        return null;
    }

    private Transfer startDownload( final String url, final Resource resource, final Transfer target, final int timeoutSeconds,
                                    final Transport transport, final boolean suppressFailures )
        throws TransferException
    {
        if ( target.exists() )
        {
            return target;
        }

        if ( transport == null )
        {
            return null;
        }

        final DownloadJob job = transport.createDownloadJob( url, resource, target, timeoutSeconds );

        final Future<Transfer> future = executor.submit( job );
        pending.put( url, future );
        try
        {
            final Transfer downloaded = future.get( timeoutSeconds, TimeUnit.SECONDS );

            if ( job.getError() != null )
            {
                logger.info( "NFC: Download error. Marking as missing: %s", resource );
                nfc.addMissing( resource );

                if ( !suppressFailures )
                {
                    throw job.getError();
                }
            }
            else if ( downloaded == null || !downloaded.exists() )
            {
                logger.info( "NFC: Download did not complete. Marking as missing: %s", resource );
                nfc.addMissing( resource );
            }

            return downloaded;
        }
        catch ( final InterruptedException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Interrupted download: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
            }
        }
        catch ( final ExecutionException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed to download: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
            }
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
            }
        }
        finally
        {
            //            logger.info( "Marking download complete: %s", url );
            pending.remove( url );
        }

        return null;
    }

}
