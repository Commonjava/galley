package org.commonjava.maven.galley.io.checksum;

import java.io.IOException;

import org.commonjava.maven.galley.io.checksum.Sha512GeneratorFactory.Sha512Generator;
import org.commonjava.maven.galley.model.Transfer;

public final class Sha512GeneratorFactory
    extends AbstractChecksumGeneratorFactory<Sha512Generator>
{

    public Sha512GeneratorFactory()
    {
    }

    @Override
    protected Sha512Generator newGenerator( final Transfer transfer )
        throws IOException
    {
        return new Sha512Generator( transfer );
    }

    public static final class Sha512Generator
        extends AbstractChecksumGenerator
    {

        protected Sha512Generator( final Transfer transfer )
            throws IOException
        {
            super( transfer, ".sha512", "SHA-512" );
        }

    }

}
