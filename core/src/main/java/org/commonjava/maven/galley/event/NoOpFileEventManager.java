package org.commonjava.maven.galley.event;

import javax.inject.Named;

import org.commonjava.maven.galley.spi.event.FileEventManager;

@Named( "no-op-galley-events" )
public class NoOpFileEventManager
    implements FileEventManager
{

    @Override
    public void fire( final FileNotFoundEvent evt )
    {
    }

    @Override
    public void fire( final FileStorageEvent evt )
    {
    }

    @Override
    public void fire( final FileAccessEvent evt )
    {
    }

    @Override
    public void fire( final FileDeletionEvent evt )
    {
    }

    @Override
    public void fire( final FileErrorEvent evt )
    {
    }

}
