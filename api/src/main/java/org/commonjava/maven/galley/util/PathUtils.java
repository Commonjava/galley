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
package org.commonjava.maven.galley.util;

public final class PathUtils
{

    public static final String ROOT = "/";

    private static final String[] ROOT_ARRY = { ROOT };

    private PathUtils()
    {
    }

    public static String[] parentPath( final String path )
    {
        final String[] parts = path.split( "/" );
        if ( parts.length < 2 )
        {
            return ROOT_ARRY;
        }
        else
        {
            final String[] parentParts = new String[parts.length - 1];
            System.arraycopy( parts, 0, parentParts, 0, parentParts.length );
            return parentParts;
        }
    }

    public static String normalize( final String... path )
    {
        if ( path == null || path.length < 1 )
        {
            return ROOT;
        }

        final StringBuilder sb = new StringBuilder();
        int idx = 0;
        parts: for ( String part : path )
        {
            if ( part == null || part.length() < 1 || "/".equals( part ) )
            {
                continue parts;
            }

            if ( idx == 0 && part.startsWith( "file:" ) )
            {
                if ( part.length() > 5 )
                {
                    sb.append( part.substring( 5 ) );
                }

                continue parts;
            }

            if ( idx > 0 )
            {
                while ( part.charAt( 0 ) == '/' )
                {
                    if ( part.length() < 2 )
                    {
                        continue parts;
                    }

                    part = part.substring( 1 );
                }
            }

            while ( part.charAt( part.length() - 1 ) == '/' )
            {
                if ( part.length() < 2 )
                {
                    continue parts;
                }

                part = part.substring( 0, part.length() - 1 );
            }

            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }

            sb.append( part );
            idx++;
        }

        if ( path[path.length - 1].endsWith( "/" ) )
        {
            sb.append( "/" );
        }

        return sb.toString();
    }

}
