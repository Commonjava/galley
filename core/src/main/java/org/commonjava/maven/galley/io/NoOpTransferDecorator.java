package org.commonjava.maven.galley.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.TransferOperation;

public class NoOpTransferDecorator
    implements TransferDecorator
{

    @Override
    public OutputStream decorateWrite( final OutputStream stream, final TransferOperation op )
        throws IOException
    {
        return stream;
    }

    @Override
    public InputStream decorateRead( final InputStream stream )
        throws IOException
    {
        return stream;
    }

}
