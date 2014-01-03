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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.VirtualResource;
import org.commonjava.maven.galley.spi.transport.LocationExpander;

@Named( "no-op-galley-location-expander" )
@Alternative
public class NoOpLocationExpander
    implements LocationExpander
{

    @SuppressWarnings( "unchecked" )
    @Override
    public <T extends Location> List<Location> expand( final Collection<T> locations )
        throws TransferException
    {
        return locations instanceof List ? (List<Location>) locations : new ArrayList<Location>( locations );
    }

    @Override
    public List<Location> expand( final Location... locations )
        throws TransferException
    {
        return Arrays.asList( locations );
    }

    @Override
    public VirtualResource expand( final Resource resource )
    {
        if ( resource instanceof VirtualResource )
        {
            return (VirtualResource) resource;
        }
        else
        {
            return new VirtualResource( Collections.singletonList( ( (ConcreteResource) resource ).getLocation() ), resource.getPath() );
        }
    }

}
