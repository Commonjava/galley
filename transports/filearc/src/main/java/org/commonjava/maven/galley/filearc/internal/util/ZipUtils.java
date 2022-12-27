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
package org.commonjava.maven.galley.filearc.internal.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ZipUtils
{

    private static final String ARCHIVE_URI_PATTERN = "(zip|jar):\\/?(\\/?file:\\/?)?(\\/[^!]+)(\\!\\/?(.*))?";

    private ZipUtils()
    {
    }

    public static boolean isJar( final String uri )
    {
        return uri.startsWith( "jar:" );
    }

    public static String getArchivePath( final String uri )
    {
        final Matcher m = Pattern.compile( ARCHIVE_URI_PATTERN )
                                 .matcher( uri );
        if ( m.matches() )
        {
            return m.group( 5 );
        }
        return null;
    }

    public static File getArchiveFile( final String uri )
    {
        final Matcher m = Pattern.compile( ARCHIVE_URI_PATTERN )
                                 .matcher( uri );
        if ( m.matches() )
        {
            return new File( m.group( 3 ) );
        }

        return null;
    }

}
