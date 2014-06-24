package org.commonjava.maven.galley.io.checksum;

import java.io.IOException;

import org.commonjava.maven.galley.io.checksum.Sha1GeneratorFactory.Sha1Generator;
import org.commonjava.maven.galley.model.Transfer;

public final class Sha1GeneratorFactory
    extends AbstractChecksumGeneratorFactory<Sha1Generator>
{

    public Sha1GeneratorFactory()
    {
    }

    @Override
    protected Sha1Generator newGenerator( final Transfer transfer )
        throws IOException
    {
        return new Sha1Generator( transfer );
    }

    public static final class Sha1Generator
        extends AbstractChecksumGenerator
    {

        protected Sha1Generator( final Transfer transfer )
            throws IOException
        {
            super( transfer, ".sha1", "SHA-1" );
        }

    }

}
