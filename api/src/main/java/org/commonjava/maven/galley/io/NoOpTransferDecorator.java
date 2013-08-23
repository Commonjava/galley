package org.commonjava.maven.galley.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

@Named( "no-op-galley-decorator" )
@Alternative
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
