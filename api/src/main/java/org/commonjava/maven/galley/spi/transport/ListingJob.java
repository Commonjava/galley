package org.commonjava.maven.galley.spi.transport;

import org.commonjava.maven.galley.model.ListingResult;

/**
 * Return null if the listing isn't found OR if there's an error.
 * 
 * @author jdcasey
 */
public interface ListingJob
    extends TransportJob<ListingResult>
{

}
