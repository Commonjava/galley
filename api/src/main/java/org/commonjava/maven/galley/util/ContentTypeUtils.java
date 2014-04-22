/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
