package org.commonjava.maven.galley.spi.io;

import org.commonjava.maven.galley.model.Location;

public interface PathGenerator
{
    String getFilePath( final Location loc, final String path );
}
