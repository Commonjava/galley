package org.commonjava.maven.galley.event;


public interface FileEventManager
{

    void fire( final FileNotFoundEvent evt );

    void fire( final FileStorageEvent evt );

    void fire( final FileAccessEvent evt );

    void fire( final FileDeletionEvent evt );

    void fire( final FileErrorEvent evt );
}
