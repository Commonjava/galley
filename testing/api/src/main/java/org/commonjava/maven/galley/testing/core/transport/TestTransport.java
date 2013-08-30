package org.commonjava.maven.galley.testing.core.transport;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Default;
import javax.inject.Named;
import javax.inject.Singleton;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.testing.core.cdi.TestData;
import org.commonjava.maven.galley.testing.core.transport.job.TestDownload;
import org.commonjava.maven.galley.testing.core.transport.job.TestListing;
import org.commonjava.maven.galley.testing.core.transport.job.TestPublish;
import org.commonjava.util.logging.Logger;

/**
 * Stubbed out {@link Transport} implementation that allows pre-registering
 * {@link DownloadJob} and {@link PublishJob} instances before attempting to 
 * access them from a higher component (such as {@link TransferManagerImpl}).
 * 
 * @author jdcasey
 */
@TestData
@Default
@Named( "test-galley-transport" )
@Singleton
public class TestTransport
    implements Transport
{
    private final Logger logger = new Logger( getClass() );

    private final Map<Resource, TestDownload> downloads = new HashMap<>();

    private final Map<String, TestPublish> publishes = new HashMap<>();

    private final Map<Resource, TestListing> listings = new HashMap<>();

    public TestTransport()
    {
    }

    /**
     * Use this to pre-register data for a {@link DownloadJob} you plan on accessing during
     * your unit test.
     */
    public void registerDownload( final Resource resource, final TestDownload job )
    {
        new Logger( getClass() ).info( "Got transport: %s", this );
        logger.info( "Registering download: %s with job: %s", resource, job );
        downloads.put( resource, job );
    }

    /**
     * Use this to pre-register the result for a {@link PublishJob} you plan on accessing during
     * your unit test.
     */
    public void registerPublish( final String url, final TestPublish job )
    {
        logger.info( "Registering publish: %s with job: %s", url, job );
        publishes.put( url, job );
    }

    public void registerListing( final Location location, final String path, final TestListing listing )
    {
        listings.put( new Resource( location, path ), listing );
    }

    // Transport implementation...

    @Override
    public DownloadJob createDownloadJob( final String url, final Resource resource, final Transfer target, final int timeoutSeconds )
        throws TransferException
    {
        final TestDownload job = downloads.get( resource );
        logger.info( "Download for: %s is: %s", resource, job );
        if ( job == null )
        {
            throw new TransferException( "No download registered for the endpoint: %s", resource );
        }

        job.setTransfer( target );
        return job;
    }

    @Override
    public PublishJob createPublishJob( final String url, final Resource resource, final InputStream stream, final long length,
                                        final int timeoutSeconds )
        throws TransferException
    {
        return createPublishJob( url, resource, stream, length, null, timeoutSeconds );
    }

    @Override
    public PublishJob createPublishJob( final String url, final Resource resource, final InputStream stream, final long length,
                                        final String contentType, final int timeoutSeconds )
        throws TransferException
    {
        final TestPublish job = publishes.get( url );
        if ( job == null )
        {
            throw new TransferException( "No publish job registered for: %s", url );
        }

        job.setContent( stream, length, contentType );
        return job;
    }

    @Override
    public boolean handles( final Location location )
    {
        return true;
    }

    @Override
    public ListingJob createListingJob( final String url, final Resource resource, final int timeoutSeconds )
        throws TransferException
    {
        final TestListing job = listings.get( resource );
        if ( job == null )
        {
            throw new TransferException( "No listing job registered for: %s", resource );
        }

        return job;
    }

}
