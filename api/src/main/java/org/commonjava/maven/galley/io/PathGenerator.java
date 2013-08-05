package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.model.Location;

public interface PathGenerator
{
    String getFilePath( final Location loc, final String path );
}
