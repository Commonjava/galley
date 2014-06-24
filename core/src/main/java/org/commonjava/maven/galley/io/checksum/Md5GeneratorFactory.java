package org.commonjava.maven.galley.io.checksum;

import java.io.IOException;

import org.commonjava.maven.galley.io.checksum.Md5GeneratorFactory.Md5Generator;
import org.commonjava.maven.galley.model.Transfer;

public final class Md5GeneratorFactory
    extends AbstractChecksumGeneratorFactory<Md5Generator>
{

    public Md5GeneratorFactory()
    {
    }

    @Override
    protected Md5Generator newGenerator( final Transfer transfer )
        throws IOException
    {
        return new Md5Generator( transfer );
    }

    public static final class Md5Generator
        extends AbstractChecksumGenerator
    {

        protected Md5Generator( final Transfer transfer )
            throws IOException
        {
            super( transfer, ".md5", "MD5" );
        }

    }

}
