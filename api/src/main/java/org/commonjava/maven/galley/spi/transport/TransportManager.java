package org.commonjava.maven.galley.spi.transport;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;

public interface TransportManager
{

    Transport getTransport( Location location )
        throws TransferException;

}
