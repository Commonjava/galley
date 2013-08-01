package org.commonjava.maven.galley.tansport;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.transport.Transport;

public interface TransportManager
{

    Transport getTransport( Location location )
        throws TransferException;

}
