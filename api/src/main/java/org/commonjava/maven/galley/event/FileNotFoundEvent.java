package org.commonjava.maven.galley.event;

import org.commonjava.maven.galley.model.Resource;

public class FileNotFoundEvent
{

    private final Resource resource;

    public FileNotFoundEvent( final Resource resource )
    {
        this.resource = resource;
    }

    public Resource getResource()
    {
        return resource;
    }
}
