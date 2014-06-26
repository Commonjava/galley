package org.commonjava.maven.galley.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

public abstract class AbstractTransferDecorator
    implements TransferDecorator
{

    protected AbstractTransferDecorator()
    {
    }

    @Override
    public OutputStream decorateWrite( final OutputStream stream, final Transfer transfer, final TransferOperation op )
        throws IOException
    {
        return stream;
    }

    @Override
    public InputStream decorateRead( final InputStream stream, final Transfer transfer )
        throws IOException
    {
        return stream;
    }

    @Override
    public void decorateTouch( final Transfer transfer )
    {
    }

    @Override
    public void decorateExists( final Transfer transfer )
    {
    }

    @Override
    public void decorateCopyFrom( final Transfer from, final Transfer transfer )
        throws IOException
    {
    }

    @Override
    public void decorateDelete( final Transfer transfer )
        throws IOException
    {
    }

    @Override
    public String[] decorateListing( final Transfer transfer, final String[] listing )
        throws IOException
    {
        return listing;
    }

    @Override
    public void decorateMkdirs( final Transfer transfer )
        throws IOException
    {
    }

    @Override
    public void decorateCreateFile( final Transfer transfer )
        throws IOException
    {
    }

}
