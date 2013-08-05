package org.commonjava.maven.galley.event;

import java.util.Collections;
import java.util.List;

import org.commonjava.maven.galley.model.Location;

public class FileNotFoundEvent
{

    private final List<? extends Location> locations;

    private final String path;

    public FileNotFoundEvent( final List<? extends Location> locations, final String path )
    {
        this.locations = locations;
        this.path = path;
    }

    public FileNotFoundEvent( final Location store, final String path )
    {
        this.locations = Collections.singletonList( store );
        this.path = path;
    }

    public List<? extends Location> getLocations()
    {
        return locations;
    }

    public String getPath()
    {
        return path;
    }

}
