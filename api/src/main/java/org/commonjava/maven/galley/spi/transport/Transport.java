package org.commonjava.maven.galley.spi.transport;

import java.io.InputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

public interface Transport
{

    /**
     * @return NEVER NULL
     */
    DownloadJob createDownloadJob( String url, Location repository, Transfer target, int timeoutSeconds )
        throws TransferException;

    /**
     * @return NEVER NULL
     */
    PublishJob createPublishJob( String url, Location repository, String path, InputStream stream, long length,
                                 int timeoutSeconds )
        throws TransferException;

    /**
     * @return NEVER NULL
     */
    PublishJob createPublishJob( String url, Location repository, String path, InputStream stream, long length,
                                 String contentType, int timeoutSeconds )
        throws TransferException;

    boolean handles( Location location );

}
