package org.commonjava.maven.galley.transport.htcli.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HttpListing
    implements ListingJob
{

    private TransferException error;

    private final int timeoutSeconds;

    private final Http http;

    private final Resource resource;

    public HttpListing( final Resource resource, final int timeoutSeconds, final Http http )
    {
        this.resource = resource;
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
        // this is used to bind credentials...
        final HttpLocation location = (HttpLocation) resource.getLocation();

        // return null if something goes wrong, after setting the error.
        // What we should be doing here is trying to retrieve the html directory 
        // listing, then parse out the filenames from that...
        //
        // They'll be links, so that's something to key in on.
        //
        // I'm wondering about this:
        // http://jsoup.org/cookbook/extracting-data/selector-syntax
        // the dependency is: org.jsoup:jsoup:1.7.2

        final HashSet<String> excludes = new HashSet<String>();
        excludes.add("../");
        
        ArrayList<String> al = new ArrayList<String>();
		
        try
        {
        	Document doc = Jsoup.connect(location.getUri()).get();
        	for (Element file : doc.select("a"))
        	{
        		if ( file.attr("href").contains(file.text()) && !excludes.contains(file.text()))
        		{
        			al.add (file.text());
        		}
        	}
        	return new ListingResult (resource, al.toArray(new String[al.size()]));
        }
        catch (IOException e)
        {
        	error = new TransferException("Caught IOException retrieving JSoup connection", e);
        }
        return null;
    }

}
