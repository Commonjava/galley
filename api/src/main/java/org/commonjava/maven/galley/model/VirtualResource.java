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
package org.commonjava.maven.galley.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VirtualResource
    extends AbstractResource
    implements Resource, Iterable<ConcreteResource>
{

    private final List<? extends Location> locations;

    public VirtualResource( final List<? extends Location> locations, final String... path )
    {
        super( path );
        this.locations = locations;
    }

    public List<ConcreteResource> toConcreteResources()
    {
        final List<ConcreteResource> result = new ArrayList<ConcreteResource>();
        for ( final Location location : locations )
        {
            result.add( new ConcreteResource( location, getPath() ) );
        }

        return result;
    }

    public List<? extends Location> getLocations()
    {
        return locations;
    }

    @Override
    public boolean allowsDownloading()
    {
        for ( final Location location : locations )
        {
            if ( location.allowsDownloading() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsPublishing()
    {
        for ( final Location location : locations )
        {
            if ( location.allowsPublishing() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsStoring()
    {
        for ( final Location location : locations )
        {
            if ( location.allowsStoring() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsSnapshots()
    {
        for ( final Location location : locations )
        {
            if ( location.allowsSnapshots() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsReleases()
    {
        for ( final Location location : locations )
        {
            if ( location.allowsReleases() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected Resource newDerivedResource( final String... path )
    {
        return new VirtualResource( locations, path );
    }

    @Override
    public Iterator<ConcreteResource> iterator()
    {
        return toConcreteResources().iterator();
    }

}
