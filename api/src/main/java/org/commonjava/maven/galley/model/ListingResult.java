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

import java.util.Arrays;
import java.util.TreeSet;

public class ListingResult
{

    private final ConcreteResource resource;

    private final String[] listing;

    public ListingResult( final ConcreteResource resource, final String[] listing )
    {
        this.resource = resource;
        this.listing = listing;
    }

    public Location getLocation()
    {
        return resource.getLocation();
    }

    public String getPath()
    {
        return resource.getPath();
    }

    public ConcreteResource getResource()
    {
        return resource;
    }

    public String[] getListing()
    {
        return listing;
    }

    public boolean isEmpty()
    {
        return listing == null || listing.length == 0;
    }

    public ListingResult mergeWith( final ListingResult remoteResult )
    {
        final TreeSet<String> merged = new TreeSet<String>();
        if ( !isEmpty() )
        {
            merged.addAll( Arrays.asList( listing ) );
        }

        if ( !remoteResult.isEmpty() )
        {
            merged.addAll( Arrays.asList( remoteResult.getListing() ) );
        }

        return new ListingResult( resource, merged.toArray( new String[merged.size()] ) );
    }

}
