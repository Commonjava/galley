/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
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
        this.transports = new ArrayList<Transport>( Arrays.asList( transports ) );
    }

    @PostConstruct
    protected void setup()
    {
        final List<Transport> transports = new ArrayList<Transport>();
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

    @Override
    public Transport getTransport( final ConcreteResource resource )
        throws TransferException
    {
        return getTransport( resource.getLocation() );
    }

}
