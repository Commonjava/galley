package org.commonjava.maven.galley.transport.htcli.internal;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

public class HttpListing
    implements ListingJob
{

    private final HttpLocation location;

    private final String path;

    private final int timeoutSeconds;

    private final Http http;

    public HttpListing( final HttpLocation location, final String path, final int timeoutSeconds, final Http http )
    {
        this.location = location;
        this.path = path;
        this.timeoutSeconds = timeoutSeconds;
        this.http = http;
    }

    @Override
    public TransferException getError()
    {
        throw new UnsupportedOperationException( "Not yet implemented!" );
    }

    @Override
    public ListingResult call()
        throws Exception
    {
        throw new UnsupportedOperationException( "Not yet implemented!" );
    }

}
