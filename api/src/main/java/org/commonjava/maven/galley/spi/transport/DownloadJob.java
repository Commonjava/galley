package org.commonjava.maven.galley.spi.transport;

import org.commonjava.maven.galley.model.Transfer;

/**
 * ONLY return null if there is an error, otherwise, return the transfer passed 
 * in, and allow the .exists() method to return false if the remote resource 
 * was not found.
 * 
 * @author jdcasey
 */
public interface DownloadJob
    extends TransportJob<Transfer>
{

}
