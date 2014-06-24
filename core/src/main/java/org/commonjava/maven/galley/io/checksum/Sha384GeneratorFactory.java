package org.commonjava.maven.galley.io.checksum;

import java.io.IOException;

import org.commonjava.maven.galley.io.checksum.Sha384GeneratorFactory.Sha384Generator;
import org.commonjava.maven.galley.model.Transfer;

public final class Sha384GeneratorFactory
    extends AbstractChecksumGeneratorFactory<Sha384Generator>
{

    public Sha384GeneratorFactory()
    {
    }

    @Override
    protected Sha384Generator newGenerator( final Transfer transfer )
        throws IOException
    {
        return new Sha384Generator( transfer );
    }

    public static final class Sha384Generator
        extends AbstractChecksumGenerator
    {

        protected Sha384Generator( final Transfer transfer )
            throws IOException
        {
            super( transfer, ".sha384", "SHA-384" );
        }

    }

}
