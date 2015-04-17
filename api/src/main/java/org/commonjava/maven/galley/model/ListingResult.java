/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
