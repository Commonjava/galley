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
package org.commonjava.maven.galley.filearc.internal.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ZipUtils
{

    private static final String ARCHIVE_URI_PATTERN = "(zip|jar):[/]{0,2}(.+\\.(jar|zip))(!.*)?";

    private static final String ARCHIVE_ENTRY_PATH_PATTERN = ".+!(.+)";

    private ZipUtils()
    {
    }

    public static boolean isJar( final String uri )
    {
        return getArchiveFile( uri ).getPath()
                                    .endsWith( ".jar" );
    }

    public static String getArchivePath( final String uri )
    {
        final Matcher m = Pattern.compile( ARCHIVE_ENTRY_PATH_PATTERN )
                                 .matcher( uri );
        if ( m.matches() )
        {
            return m.group( 1 );
        }

        return null;
    }

    public static File getArchiveFile( final String uri )
    {
        final Matcher m = Pattern.compile( ARCHIVE_URI_PATTERN )
                                 .matcher( uri );
        if ( m.matches() )
        {
            return new File( m.group( 2 ) );
        }

        return null;
    }

}
