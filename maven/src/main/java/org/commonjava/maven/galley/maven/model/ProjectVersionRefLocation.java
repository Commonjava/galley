package org.commonjava.maven.galley.maven.model;

import java.util.Map.Entry;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.version.SingleVersion;
import org.commonjava.maven.galley.model.Location;

public final class ProjectVersionRefLocation
{

    private final Location location;

    private final ProjectVersionRef ref;

    public ProjectVersionRefLocation( final ProjectVersionRef ref, final Location location )
    {
        this.ref = ref;
        this.location = location;
    }

    public ProjectVersionRefLocation( final ProjectVersionRef original, final Entry<SingleVersion, Location> entry )
    {
        this.ref = original.selectVersion( entry.getKey() );
        this.location = entry.getValue();
    }

    public Location getLocation()
    {
        return location;
    }

    public ProjectVersionRef getRef()
    {
        return ref;
    }

}
