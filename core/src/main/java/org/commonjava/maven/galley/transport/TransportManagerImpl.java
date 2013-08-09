package org.commonjava.maven.galley.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;

public class TransportManagerImpl
    implements TransportManager
{

    @Inject
    private Instance<Transport> injected;

    private List<Transport> transports;

    protected TransportManagerImpl()
    {
    }

    public TransportManagerImpl( final Transport... transports )
    {
        this.transports = new ArrayList<>( Arrays.asList( transports ) );
    }

    protected void setup()
    {
        final List<Transport> transports = new ArrayList<>();
        if ( injected != null )
        {
            for ( final Transport transport : injected )
            {
                transports.add( transport );
            }
        }

        this.transports = transports;
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
