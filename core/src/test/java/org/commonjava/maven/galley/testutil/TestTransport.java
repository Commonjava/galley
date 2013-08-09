package org.commonjava.maven.galley.testutil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferManagerImpl;
import org.commonjava.maven.galley.live.testutil.TestData;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;
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

    private final Map<TestEndpoint, TestDownloadJob> downloads = new HashMap<>();

    private final Map<String, TestPublishJob> publishes = new HashMap<>();

    public TestTransport()
    {
    }

    /**
     * Use this to pre-register data for a {@link DownloadJob} you plan on accessing during
     * your unit test.
     */
    public void registerDownload( final Location loc, final String path, final TestDownloadJob job )
    {
        final TestEndpoint end = new TestEndpoint( loc, path );
        new Logger( getClass() ).info( "Got transport: %s", this );
        logger.info( "Registering download: %s with job: %s", end, job );
        downloads.put( end, job );
    }

    /**
     * Use this to pre-register the result for a {@link PublishJob} you plan on accessing during
     * your unit test.
     */
    public void registerPublish( final String url, final TestPublishJob job )
    {
        logger.info( "Registering publish: %s with job: %s", url, job );
        publishes.put( url, job );
    }

    // Transport implementation...

    @Override
    public DownloadJob createDownloadJob( final String url, final Location repository, final Transfer target,
                                          final int timeoutSeconds )
        throws TransferException
    {
        final TestEndpoint end = new TestEndpoint( target.getLocation(), target.getPath() );
        final TestDownloadJob job = downloads.get( end );
        logger.info( "Download for: %s is: %s", end, job );
        if ( job == null )
        {
            throw new TransferException( "No download registered for the endpoint: %s", end );
        }

        job.setTransfer( target );
        return job;
    }

    @Override
    public PublishJob createPublishJob( final String url, final Location repository, final String path,
                                        final InputStream stream, final long length, final int timeoutSeconds )
        throws TransferException
    {
        return createPublishJob( url, repository, path, stream, length, null, timeoutSeconds );
    }

    @Override
    public PublishJob createPublishJob( final String url, final Location repository, final String path,
                                        final InputStream stream, final long length, final String contentType,
                                        final int timeoutSeconds )
        throws TransferException
    {
        final TestPublishJob job = publishes.get( url );
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

}
