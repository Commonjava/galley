package org.commonjava.maven.galley.util;

import static org.apache.commons.lang.StringUtils.join;

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

        String result = join( path, "/" );
        while ( result.startsWith( "/" ) && result.length() > 1 )
        {
            result = result.substring( 1 );
        }

        return result;
    }

}
