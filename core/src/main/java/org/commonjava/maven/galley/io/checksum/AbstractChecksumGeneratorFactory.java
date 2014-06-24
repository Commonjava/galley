package org.commonjava.maven.galley.io.checksum;

import java.io.IOException;

import org.commonjava.maven.galley.model.Transfer;

public abstract class AbstractChecksumGeneratorFactory<T extends AbstractChecksumGenerator>
{

    protected AbstractChecksumGeneratorFactory()
    {
    }

    public final T createGenerator( final Transfer transfer )
        throws IOException
    {
        return newGenerator( transfer );
    }

    protected abstract T newGenerator( Transfer transfer )
        throws IOException;

}
