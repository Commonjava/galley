package org.commonjava.maven.galley.spi.transport;

import java.io.InputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;

public interface Transport
{

    /**
     * @return NEVER NULL
     */
    ListingJob createListingJob( Resource resource, int timeoutSeconds )
        throws TransferException;

    /**
     * @return NEVER NULL
     */
    DownloadJob createDownloadJob( String url, Resource resource, Transfer target, int timeoutSeconds )
        throws TransferException;

    /**
     * @return NEVER NULL
     */
    PublishJob createPublishJob( String url, Resource resource, InputStream stream, long length, int timeoutSeconds )
        throws TransferException;

    /**
     * @return NEVER NULL
     */
    PublishJob createPublishJob( String url, Resource resource, InputStream stream, long length, String contentType,
                                 int timeoutSeconds )
        throws TransferException;

    boolean handles( Location location );

}
