package org.commonjava.maven.galley.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.commonjava.maven.galley.event.FileAccessEvent;
import org.commonjava.maven.galley.spi.event.FileEventManager;

public class TransferInputStream
    extends FilterInputStream
{

    private final FileAccessEvent event;

    private final FileEventManager fileEventManager;

    public TransferInputStream( final InputStream in, final FileAccessEvent event,
                                final FileEventManager fileEventManager )
    {
        super( in );
        this.event = event;
        this.fileEventManager = fileEventManager;
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();
        fileEventManager.fire( event );
    }

}
