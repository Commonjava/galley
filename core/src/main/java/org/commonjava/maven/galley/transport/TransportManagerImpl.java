/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;

@ApplicationScoped
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

    public TransportManagerImpl( final List<Transport> transports )
    {
        this.transports = transports;
    }

    @PostConstruct
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

    @SuppressWarnings( "RedundantThrows" )
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
