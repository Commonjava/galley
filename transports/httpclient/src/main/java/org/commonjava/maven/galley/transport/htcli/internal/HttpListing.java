/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.transport.htcli.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpListing
    implements ListingJob
{

    private static final Set<String> EXCLUDES = new HashSet<String>()
    {
        private static final long serialVersionUID = 1L;

        {
            add( "../" );
        }
    };

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private TransferException error;

    private final Http http;

    private final ConcreteResource resource;

    private final String url;

    public HttpListing( final String url, final ConcreteResource resource, final int timeoutSeconds, final Http http )
    {
        this.url = url;
        this.resource = resource;
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

        //            logger.info( "Trying: {}", url );
        final HttpGet request = new HttpGet( url );

        http.bindCredentialsTo( location, request );

        // return null if something goes wrong, after setting the error.
        // What we should be doing here is trying to retrieve the html directory 
        // listing, then parse out the filenames from that...
        //
        // They'll be links, so that's something to key in on.
        //
        // I'm wondering about this:
        // http://jsoup.org/cookbook/extracting-data/selector-syntax
        // the dependency is: org.jsoup:jsoup:1.7.2

        ListingResult result = null;
        try
        {
            final InputStream in = executeGet( request, url );

            if ( in != null )
            {
                final ArrayList<String> al = new ArrayList<String>();

                // TODO: Charset!!
                final Document doc = Jsoup.parse( in, "UTF-8", url );
                for ( final Element file : doc.select( "a" ) )
                {
                    if ( file.attr( "href" )
                             .contains( file.text() ) && !EXCLUDES.contains( file.text() ) )
                    {
                        al.add( file.text() );
                    }
                }

                result = new ListingResult( resource, al.toArray( new String[al.size()] ) );
            }
        }
        catch ( final TransferException e )
        {
            this.error = e;
        }
        catch ( final IOException e )
        {
            this.error = new TransferException( "Failed to parse directory listing HTML for: {} using JSoup. Reason: {}", e, url, e.getMessage() );
        }
        finally
        {
            cleanup( location, request );
        }

        return error == null ? result : null;
    }

    private InputStream executeGet( final HttpGet request, final String url )
        throws TransferException
    {
        InputStream result = null;

        try
        {
            final HttpResponse response = http.getClient()
                                              .execute( request );
            final StatusLine line = response.getStatusLine();
            final int sc = line.getStatusCode();
            if ( sc != HttpStatus.SC_OK )
            {
                logger.warn( "{} : {}", line, url );
                if ( sc == HttpStatus.SC_NOT_FOUND )
                {
                    result = null;
                }
                else
                {
                    throw new TransferException( "HTTP request failed: %s", line );
                }
            }
            else
            {
                result = response.getEntity()
                                 .getContent();
            }
        }
        catch ( final ClientProtocolException e )
        {
            throw new TransferException( "Repository remote request failed for: %s. Reason: %s", e, url, e.getMessage() );
        }
        catch ( final IOException e )
        {
            throw new TransferException( "Repository remote request failed for: %s. Reason: %s", e, url, e.getMessage() );
        }

        return result;
    }

    private void cleanup( final HttpLocation location, final HttpGet request )
    {
        http.clearBoundCredentials( location );
        request.abort();
        http.closeConnection();
    }

}
