package org.commonjava.maven.galley.util;

import javax.activation.MimetypesFileTypeMap;

public final class ContentTypeUtils
{

    private ContentTypeUtils()
    {
    }

    public static String detectContent( final String path )
    {
        if ( path.endsWith( ".jar" ) || path.endsWith( ".war" ) || path.endsWith( ".ear" ) )
        {
            return "application/java-archive";
        }
        else if ( path.endsWith( ".xml" ) || path.endsWith( ".pom" ) )
        {
            return "application/xml";
        }
        else if ( path.endsWith( ".json" ) )
        {
            return "application/json";
        }

        return MimetypesFileTypeMap.getDefaultFileTypeMap()
                                   .getContentType( path );
    }
}
