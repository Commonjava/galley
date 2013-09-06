package org.commonjava.maven.galley.internal.xfer;

import static org.commonjava.maven.galley.util.UrlUtils.buildUrl;

import java.net.MalformedURLException;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.util.logging.Logger;

@ApplicationScoped
public class ListingHandler
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private NotFoundCache nfc;

    public ListingHandler()
    {
    }

    public ListingHandler( final NotFoundCache nfc )
    {
        this.nfc = nfc;
    }

    public ListingResult list( final Resource resource, final int timeoutSeconds, final Transport transport, final boolean suppressFailures )
        throws TransferException
    {
        if ( nfc.isMissing( resource ) )
        {
            logger.info( "NFC: Already marked as missing: %s", resource );
            return null;
        }

        if ( transport == null )
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

        logger.info( "LIST %s", url );

        final ListingJob job = transport.createListingJob( url, resource, timeoutSeconds );

        // TODO: execute this stuff in a thread just like downloads/publishes. Requires cache storage...
        try
        {
            final ListingResult result = job.call();

            if ( job.getError() != null )
            {
                logger.info( "NFC: Download error. Marking as missing: %s", resource );
                nfc.addMissing( resource );

                if ( !suppressFailures )
                {
                    throw job.getError();
                }
            }
            else if ( result == null )
            {
                logger.info( "NFC: Download did not complete. Marking as missing: %s", resource );
                nfc.addMissing( resource );
            }

            return result;
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: %s. Reason: %s", e, resource, e.getMessage() );
            }
        }
        catch ( final Exception e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed listing: %s. Reason: %s", e, resource, e.getMessage() );
            }
        }

        return null;
    }

}
