package org.commonjava.maven.galley.transport.htcli.internal;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

public class HttpListing
    implements ListingJob
{

    private TransferException error;

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
        // this would have been set during the call() method's execution if something went wrong.
        return error;
    }

    @Override
    public ListingResult call()
    {
        // return null if something goes wrong, after setting the error.
        // What we should be doing here is trying to retrieve the html directory 
        // listing, then parse out the filenames from that...
        //
        // They'll be links, so that's something to key in on.
        //
        // I'm wondering about this:
        // http://jsoup.org/cookbook/extracting-data/selector-syntax
        // the dependency is: org.jsoup:jsoup:1.7.2
        throw new UnsupportedOperationException( "Not yet implemented!" );
    }

}
