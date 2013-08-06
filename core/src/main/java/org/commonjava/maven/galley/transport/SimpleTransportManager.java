package org.commonjava.maven.galley.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;

public class SimpleTransportManager
    implements TransportManager
{

    private final List<Transport> transports;

    public SimpleTransportManager( final Transport... transports )
    {
        this.transports = new ArrayList<>( Arrays.asList( transports ) );
    }

    @Override
    public Transport getTransport( final Location location )
        throws TransferException
    {
        for ( final Transport t : transports )
        {
            if ( t.handles( location ) )
            {
                return t;
            }
        }

        return null;
    }

}
