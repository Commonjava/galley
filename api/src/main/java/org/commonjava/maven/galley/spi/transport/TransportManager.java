package org.commonjava.maven.galley.spi.transport;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;

public interface TransportManager
{

    Transport getTransport( Location location )
        throws TransferException;

    Transport getTransport( Resource resource )
        throws TransferException;

}
