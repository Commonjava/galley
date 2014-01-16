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
        if ( parts.length == 1 )
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
        for ( String part : path )
        {
            if ( part.length() < 1 || "/".equals( part ) )
            {
                continue;
            }

            if ( idx == 0 && part.startsWith( "file:" ) )
            {
                if ( part.length() > 5 )
                {
                    sb.append( part.substring( 5 ) );
                }

                continue;
            }

            if ( idx > 0 )
            {
                while ( part.charAt( 0 ) == '/' )
                {
                    if ( part.length() < 2 )
                    {
                        continue;
                    }

                    part = part.substring( 1 );
                }
            }

            while ( part.charAt( part.length() - 1 ) == '/' )
            {
                if ( part.length() < 2 )
                {
                    continue;
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

        return sb.toString();
    }

}
