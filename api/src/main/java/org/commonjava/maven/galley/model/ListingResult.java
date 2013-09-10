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

    public Resource getResource()
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
        final TreeSet<String> merged = new TreeSet<>();
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
