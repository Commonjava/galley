package org.commonjava.maven.galley.filearc.internal;

import java.io.File;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.transport.ListingJob;

public class FileListing
    implements ListingJob
{

    private TransferException error;

    private final File src;

    private final Location location;

    private final String path;

    public FileListing( final Location location, final String path, final File src )
    {
        this.location = location;
        this.path = path;
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
        if ( src.exists() && !src.isDirectory() )
        {
            return new ListingResult( location, path, src.list() );
        }

        return null;
    }

}
