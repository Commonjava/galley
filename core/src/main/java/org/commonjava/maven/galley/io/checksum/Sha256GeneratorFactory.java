package org.commonjava.maven.galley.io.checksum;

import java.io.IOException;

import org.commonjava.maven.galley.io.checksum.Sha256GeneratorFactory.Sha256Generator;
import org.commonjava.maven.galley.model.Transfer;

public final class Sha256GeneratorFactory
    extends AbstractChecksumGeneratorFactory<Sha256Generator>
{

    public Sha256GeneratorFactory()
    {
    }

    @Override
    protected Sha256Generator newGenerator( final Transfer transfer )
        throws IOException
    {
        return new Sha256Generator( transfer );
    }

    public static final class Sha256Generator
        extends AbstractChecksumGenerator
    {

        protected Sha256Generator( final Transfer transfer )
            throws IOException
        {
            super( transfer, ".sha256", "SHA-256" );
        }

    }

}
