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
        final List<ConcreteResource> result = new ArrayList<>();
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
