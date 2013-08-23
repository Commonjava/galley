package org.commonjava.maven.galley.model;

import java.util.Arrays;
import java.util.TreeSet;

public class ListingResult
{

    private final Location location;

    private final String path;

    private final String[] listing;

    public ListingResult( final Location location, final String path, final String[] listing )
    {
        this.location = location;
        this.path = path;
        this.listing = listing;
    }

    public Location getLocation()
    {
        return location;
    }

    public String getPath()
    {
        return path;
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

        return new ListingResult( location, path, merged.toArray( new String[merged.size()] ) );
    }

}
