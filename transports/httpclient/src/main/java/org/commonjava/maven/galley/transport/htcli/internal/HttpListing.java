/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.transport.htcli.internal;

import static org.apache.commons.io.IOUtils.closeQuietly;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferLocationException;
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
    extends AbstractHttpJob
    implements ListingJob
{

    private static final Set<String> EXCLUDES = new HashSet<String>()
    {
        private static final long serialVersionUID = 1L;

        {
            add( "../" );
        }
    };

    private final ConcreteResource resource;

    public HttpListing( final String url, final ConcreteResource resource, final Http http )
    {
        super( url, (HttpLocation) resource.getLocation(), http );
        this.resource = resource;
    }

    @Override
    public ListingResult call()
    {
        request = new HttpGet( url );

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
        OutputStream stream = null;
        InputStream in = null;
        try
        {
            if ( executeHttp() )
            {
                in = response.getEntity().getContent();
                String listing = IOUtils.toString( in );
                Logger logger = LoggerFactory.getLogger( getClass() );
                logger.debug( "Got raw listing content:\n\n{}\n\n", listing );

                final ArrayList<String> al = new ArrayList<String>();

                // TODO: Charset!!
                Document doc = Jsoup.parse( listing, url );
//                try
//                {
//                }
//                catch ( final IOException e )
//                {
//                    this.error =
//                            new TransferLocationException( resource.getLocation(), "Invalid HTML in: {}. Reason: {}", e, url, e.getMessage() );
//                }

                if ( doc != null )
                {
                    for ( final Element link : doc.select( "a" ) )
                    {
                        String linkText = link.text();
                        String linkHref = link.attr( "href" );

                        URL url = new URL( this.url );

                        boolean sameServer = isSameServer( url, linkHref );
                        boolean subpath = isSubpath( url, linkHref );

                        if ( ( sameServer && subpath )
                                && ( linkHref.endsWith( linkText ) || linkHref.endsWith( linkText + '/' ) )
                                && !EXCLUDES.contains( linkText ) )
                        {
                            al.add( linkText );
                        }
                    }

                    result = new ListingResult( resource, al.toArray( new String[al.size()] ) );
                }
            }
        }
        catch ( final TransferException e )
        {
            this.error = e;
        }
        catch ( final IOException e )
        {
            this.error =
                new TransferException( "Failed to construct directory listing for: {}. Reason: {}", e,
                                       url, e.getMessage() );
        }
        finally
        {
            closeQuietly( in );
            closeQuietly( stream );
            cleanup();
        }

        return error == null ? result : null;
    }

    static boolean isSubpath( final URL url, final String linkHref )
    {
        String linkPath;
        try
        {
            URL linkUrl = new URL( linkHref );
            linkPath = linkUrl.getPath();
        }
        catch ( MalformedURLException ex )
        {
            linkPath = linkHref;
        }

        boolean valid = linkPath.length() > 0
               && ( ( ( linkPath.charAt( 0 ) != '/' ) && ( linkPath.charAt( 0 ) != '.' ) )
                    || linkPath.startsWith( url.getPath() ) );

        Logger logger = LoggerFactory.getLogger( HttpListing.class );
        logger.debug( "Does URL: {} (linkPath: {}) reference a sub-path of: {}? {}", linkHref, linkPath, url.getPath(),
                      valid );
        return valid;
    }

    static boolean isSameServer( final URL url, final String linkHref )
    {
        String linkProtocol = null;
        String linkAuthority = null;
        Logger logger = LoggerFactory.getLogger( HttpListing.class );
        try
        {
            URL linkUrl = new URL( linkHref );
            linkProtocol = linkUrl.getProtocol();
            linkAuthority = linkUrl.getAuthority();

            logger.debug( "Absolute URL: {} is on the same server.", linkHref );
        }
        catch ( MalformedURLException ex )
        {
            // linkHref is a relative path on the same server
            logger.debug( "URL is relative, must be on the same server." );
        }

        boolean valid = ( linkProtocol == null || linkProtocol.equals( url.getProtocol() ) )
               && ( linkAuthority == null || linkAuthority.equals( url.getAuthority() ) );

        logger.debug( "URL: {} has same protocol and authority? {}", linkHref, valid );

        return valid;
    }

}
