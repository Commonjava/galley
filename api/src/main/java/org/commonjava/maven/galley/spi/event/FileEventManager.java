package org.commonjava.maven.galley.spi.event;

import org.commonjava.maven.galley.event.FileAccessEvent;
import org.commonjava.maven.galley.event.FileDeletionEvent;
import org.commonjava.maven.galley.event.FileErrorEvent;
import org.commonjava.maven.galley.event.FileNotFoundEvent;
import org.commonjava.maven.galley.event.FileStorageEvent;


public interface FileEventManager
{

    void fire( final FileNotFoundEvent evt );

    void fire( final FileStorageEvent evt );

    void fire( final FileAccessEvent evt );

    void fire( final FileDeletionEvent evt );

    void fire( final FileErrorEvent evt );
}
