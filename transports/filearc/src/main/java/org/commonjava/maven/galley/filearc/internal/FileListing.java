package org.commonjava.maven.galley.filearc.internal;

import java.io.File;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.spi.transport.ListingJob;

public class FileListing
    implements ListingJob
{

    private TransferException error;

    private final File src;

    private final Resource resource;

    public FileListing( final Resource resource, final File src )
    {
        this.resource = resource;
        this.src = src;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public ListingResult call()
    {
        if ( src.canRead() && src.isDirectory() )
        {
            return new ListingResult( resource, src.list() );
        }

        return null;
    }

}
