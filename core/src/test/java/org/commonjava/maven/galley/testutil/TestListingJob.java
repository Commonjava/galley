package org.commonjava.maven.galley.testutil;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.spi.transport.ListingJob;

public class TestListingJob
    implements ListingJob
{

    private final TransferException error;

    private final ListingResult result;

    public TestListingJob( final TransferException error )
    {
        this.error = error;
        this.result = null;
    }

    public TestListingJob( final ListingResult result )
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
