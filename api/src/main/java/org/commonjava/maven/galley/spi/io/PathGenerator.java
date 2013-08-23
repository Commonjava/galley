package org.commonjava.maven.galley.spi.io;

import org.commonjava.maven.galley.model.Resource;

public interface PathGenerator
{
    String getFilePath( final Resource resource );
}
