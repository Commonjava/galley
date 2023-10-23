/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
package org.commonjava.maven.galley.util;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.commonjava.maven.galley.util.LocationUtils.ATTR_PATH_ENCODE;
import static org.commonjava.maven.galley.util.LocationUtils.PATH_ENCODE_BASE64;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UrlUtils
{
    final static Logger logger = LoggerFactory.getLogger( UrlUtils.class );

    private UrlUtils()
    {
    }

    public static String stringQueryParameter( final Object value )
    {
        final String base = String.valueOf( value );
        return "%22" + base + "%22";
    }

    public static String siblingDatabaseUrl( final String dbUrl, final String siblingName )
    {
        if ( isEmpty( dbUrl ) )
        {
            throw new IllegalArgumentException( "Cannot calculate sibling database URL based on empty or null database URL." );
        }

        final StringBuilder sb = new StringBuilder();
        final int protoIdx = dbUrl.indexOf( "://" ) + 3;

        final int lastIdx;
        if ( dbUrl.charAt( dbUrl.length() - 1 ) == '/' )
        {
            lastIdx = dbUrl.lastIndexOf( '/', dbUrl.length() - 2 );
        }
        else
        {
            lastIdx = dbUrl.lastIndexOf( '/' );
        }

        if ( lastIdx > protoIdx )
        {
            sb.append( dbUrl, 0, lastIdx + 1 )
              .append( siblingName );

            return sb.toString();
        }

        throw new IllegalArgumentException( "Cannot calculate sibling database URL for: '" + dbUrl + "' (cannot find last path separator '/')" );
    }

    public static String buildUrl( final String baseUrl, final String... parts )
        throws MalformedURLException
    {
        return buildUrl( baseUrl, null, parts );
    }

    public static String buildUrl( final ConcreteResource resource )
        throws MalformedURLException
    {
        final String remoteBase = resource.getLocationUri();
        if ( remoteBase == null )
        {
            return null;
        }
        String path = resource.getPath();
        // check if repo use base64 to encode 'path + query parameters'
        if (PATH_ENCODE_BASE64.equals( resource.getLocation().getAttribute(ATTR_PATH_ENCODE, String.class) ))
        {
            String p = path.replaceAll("^/*", ""); // remove leading slash if any
            // decode it (this seamlessly handles data encoded in URL-safe or normal mode)
            path = new String(Base64.decodeBase64(p));
            logger.debug("Build url, path: {}, base64 decoded: {}", p, path);
        }
        return buildUrl( remoteBase, path );
    }

    public static String buildUrl( final String baseUrl, final Map<String, String> params, final String... parts )
        throws MalformedURLException
    {
        if ( parts == null || parts.length < 1 )
        {
            return baseUrl;
        }

        final StringBuilder urlBuilder = new StringBuilder();

        if ( parts[0] == null || !parts[0].startsWith( baseUrl ) )
        {
            urlBuilder.append( baseUrl );
        }

        for ( String part : parts )
        {
            if ( part == null || part.trim()
                                     .length() < 1 )
            {
                continue;
            }

            if ( part.startsWith( "/" ) )
            {
                part = part.substring( 1 );
            }

            if ( urlBuilder.length() > 0 && urlBuilder.charAt( urlBuilder.length() - 1 ) != '/' )
            {
                urlBuilder.append( "/" );
            }

            urlBuilder.append( part );
        }

        if ( params != null && !params.isEmpty() )
        {
            urlBuilder.append( "?" );
            boolean first = true;
            for ( final Map.Entry<String, String> param : params.entrySet() )
            {
                if ( first )
                {
                    first = false;
                }
                else
                {
                    urlBuilder.append( "&" );
                }

                urlBuilder.append( param.getKey() )
                          .append( "=" )
                          .append( param.getValue() );
            }
        }

        return new URL( urlBuilder.toString() ).toExternalForm();
    }

}
