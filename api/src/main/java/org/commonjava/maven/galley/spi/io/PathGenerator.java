package org.commonjava.maven.galley.spi.io;

import org.commonjava.maven.galley.model.ConcreteResource;

public interface PathGenerator
{
    String getFilePath( final ConcreteResource resource );
}
