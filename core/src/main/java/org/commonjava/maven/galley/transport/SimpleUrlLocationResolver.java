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
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;

/**
 * Implementation of {@link LocationResolver} that constructs a {@link SimpleLocation}, then uses the following
 * validation logic:<br/>
 * 
 * <ol>
 *   <li>Use the provided {@link LocationExpander} to expand to one or more concrete locations, or return 
 *          null if none are returned.</li>
 *   <li>Use the provided {@link TransportManager} to validate that all expanded locations are valid.</li>
 *   <li>Return the list of expanded locations</li>
 * </ol>
 * 
 * @author jdcasey
 */
@Alternative
@Named
public class SimpleUrlLocationResolver
    implements LocationResolver
{

    @Inject
    private LocationExpander locationExpander;

    @Inject
    private TransportManager transportManager;

    @SuppressWarnings( "unused" )
    protected SimpleUrlLocationResolver()
    {
    }

    public SimpleUrlLocationResolver( final LocationExpander locationExpander, final TransportManager transportManager )
    {
        this.locationExpander = locationExpander;
        this.transportManager = transportManager;
    }
    
    @Override
    public final Location resolve( final String spec )
        throws TransferException
    {
        final Location location = new SimpleLocation( spec );
        final List<Location> locations = locationExpander.expand( location );
        if ( locations == null || locations.isEmpty() )
        {
            throw new TransferException( "Invalid location: '%s'. Location expansion returned no results.", spec );
        }

        for ( final Iterator<Location> iterator = new ArrayList<>( locations ).iterator(); iterator.hasNext(); )
        {
            final Location loc = iterator.next();

            // normally, this will probably throw an exception if no transport is available.
            // in case it's not, remove the location if the transport is null.
            final Transport transport = transportManager.getTransport( loc );
            if ( transport == null )
            {
                iterator.remove();
            }
        }

        if ( locations.isEmpty() )
        {
            throw new TransferException( "Invalid location: '%s'. No transports available for expanded locations.",
                                         spec );
        }

        return location;
    }

}
