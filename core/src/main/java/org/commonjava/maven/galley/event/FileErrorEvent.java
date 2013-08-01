package org.commonjava.maven.galley.event;

import org.commonjava.maven.galley.model.Transfer;

public class FileErrorEvent
    extends FileEvent
{
    private final Throwable error;

    public FileErrorEvent( final Transfer transfer, final Throwable error )
    {
        super( transfer );
        this.error = error;
    }

    public Throwable getError()
    {
        return error;
    }

}
