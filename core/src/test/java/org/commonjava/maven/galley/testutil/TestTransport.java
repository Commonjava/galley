package org.commonjava.maven.galley.testutil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;

/**
 * Stubbed out {@link Transport} implementation that allows pre-registering
 * {@link DownloadJob} and {@link PublishJob} instances before attempting to 
 * access them from a higher component (such as {@link TransferManager}).
 * 
 * @author jdcasey
 */
public class TestTransport
    implements Transport
{

    private final Map<Transfer, DownloadJob> downloads = new HashMap<>();

    private final Map<String, PublishJob> publishes = new HashMap<>();

    /**
     * Use this to pre-register a {@link DownloadJob} you plan on accessing during
     * your unit test.
     * 
     * @return any {@link DownloadJob} previously registered to that location and path. 
     */
    public DownloadJob registerDownload( final Transfer txfr, final DownloadJob job )
    {
        return downloads.put( txfr, job );
    }

    /**
     * Use this to pre-register a {@link PublishJob} you plan on accessing during
     * your unit test.
     * 
     * @return any {@link PublishJob} previously registered to that location and path. 
     */
    public PublishJob registerPublish( final String url, final PublishJob job )
    {
        return publishes.put( url, job );
    }

    // Transport implementation...

    @Override
    public DownloadJob createDownloadJob( final String url, final Location repository, final Transfer target,
                                          final int timeoutSeconds )
        throws TransferException
    {
        return downloads.get( target );
    }

    @Override
    public PublishJob createPublishJob( final String url, final Location repository, final String path,
                                        final InputStream stream, final long length, final int timeoutSeconds )
        throws TransferException
    {
        return publishes.get( url );
    }

    @Override
    public PublishJob createPublishJob( final String url, final Location repository, final String path,
                                        final InputStream stream, final long length, final String contentType,
                                        final int timeoutSeconds )
        throws TransferException
    {
        return publishes.get( url );
    }

    @Override
    public boolean handles( final Location location )
    {
        return true;
    }

}
