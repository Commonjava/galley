package org.commonjava.maven.galley.testing.core.transport.job;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.spi.transport.ListingJob;

public class TestListing
    implements ListingJob
{

    private final TransferException error;

    private final ListingResult result;

    public TestListing( final TransferException error )
    {
        this.error = error;
        this.result = null;
    }

    public TestListing( final ListingResult result )
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
    public ListingResult call()
        throws Exception
    {
        return result;
    }

}
