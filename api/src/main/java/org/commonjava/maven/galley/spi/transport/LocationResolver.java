package org.commonjava.maven.galley.spi.transport;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;

/**
 * Given a String representation of a location, resolve to a list of {@link Location} objects. This will
 * normally involve using a {@link LocationExpander} to resolve any virtualized locations, then using something
 * like a {@link TransportManager} to validate the reachability of the {@link Location}s. If validation
 * succeeds, return the potentially virtual location originally created.<br/>
 * 
 * This component isn't currently used anywhere within the Galley infrastructure, but is instead provided for
 * use by Galley consumers in order to make it simpler to use Galley itself.
 * 
 * @author jdcasey
 */
public interface LocationResolver
{

    Location resolve( String spec )
        throws TransferException;

}
