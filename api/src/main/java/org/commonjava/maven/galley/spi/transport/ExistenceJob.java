package org.commonjava.maven.galley.spi.transport;


/**
 * Return null if the listing isn't found OR if there's an error.
 * 
 * @author jdcasey
 */
public interface ExistenceJob
    extends TransportJob<Boolean>
{

}
