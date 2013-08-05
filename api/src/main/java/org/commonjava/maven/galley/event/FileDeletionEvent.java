package org.commonjava.maven.galley.event;

import org.commonjava.maven.galley.model.Transfer;

public class FileDeletionEvent
    extends FileEvent
{
    public FileDeletionEvent( final Transfer transfer )
    {
        super( transfer );
    }

}
