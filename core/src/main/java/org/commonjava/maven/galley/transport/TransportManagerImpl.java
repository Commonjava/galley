/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
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
