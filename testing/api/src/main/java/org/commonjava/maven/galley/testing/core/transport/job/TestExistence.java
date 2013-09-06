package org.commonjava.maven.galley.testing.core.transport.job;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;

public class TestExistence
    implements ExistenceJob
{

    private final TransferException error;

    private final Boolean result;

    public TestExistence( final TransferException error )
    {
        this.error = error;
        this.result = null;
    }

    public TestExistence( final boolean result )
    {
        this.result = result;
        this.error = null;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Boolean call()
        throws Exception
    {
        return result;
    }

}
