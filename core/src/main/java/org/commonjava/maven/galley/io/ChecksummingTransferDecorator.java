package org.commonjava.maven.galley.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.galley.io.checksum.AbstractChecksumGeneratorFactory;
import org.commonjava.maven.galley.io.checksum.ChecksummingOutputStream;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

public final class ChecksummingTransferDecorator
    implements TransferDecorator
{

    private final Set<AbstractChecksumGeneratorFactory<?>> checksumFactories;

    private final Set<TransferOperation> ops;

    public ChecksummingTransferDecorator( final Set<TransferOperation> ops,
                                          final AbstractChecksumGeneratorFactory<?>... checksumFactories )
    {
        this.ops = ops;
        this.checksumFactories = new HashSet<AbstractChecksumGeneratorFactory<?>>( Arrays.asList( checksumFactories ) );
    }

    public ChecksummingTransferDecorator( final Set<TransferOperation> ops,
                                          final Collection<AbstractChecksumGeneratorFactory<?>> checksumFactories )
    {
        this.ops = ops;
        if ( checksumFactories instanceof Set )
        {
            this.checksumFactories = (Set<AbstractChecksumGeneratorFactory<?>>) checksumFactories;
        }
        else
        {
            this.checksumFactories = new HashSet<AbstractChecksumGeneratorFactory<?>>( checksumFactories );
        }
    }

    @Override
    public OutputStream decorateWrite( final OutputStream stream, final Transfer transfer, final TransferOperation op )
        throws IOException
    {
        if ( ops.contains( op ) )
        {
            return new ChecksummingOutputStream( checksumFactories, stream, transfer );
        }

        return stream;
    }

    @Override
    public InputStream decorateRead( final InputStream stream, final Transfer transfer )
        throws IOException
    {
        return stream;
    }
}
