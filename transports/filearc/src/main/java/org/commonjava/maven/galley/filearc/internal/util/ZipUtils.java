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
