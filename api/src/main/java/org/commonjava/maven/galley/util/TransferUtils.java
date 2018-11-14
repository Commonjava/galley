/**
 * Copyright (C) 2013 Red Hat, Inc.
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

import org.commonjava.atlas.maven.ident.util.SnapshotUtils;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransferUtils
{
    private static final Logger logger = LoggerFactory.getLogger( TransferUtils.class );

    /**
     * Is a transfer should be filtered out based on transfer's metadata of allow snapshots and allow releases?
     *
     * @param transfer
     * @return true - filtered out; false - not filtered
     */
    public static boolean filterTransfer( final Transfer transfer )
    {
        final Location loc = transfer.getLocation();
        final boolean allowsSnapshots = loc.allowsSnapshots();
        final boolean allowsReleases = loc.allowsReleases();
        if ( !allowsSnapshots || !allowsReleases )
        {
            if ( transfer.isFile() )
            {
                final String path = transfer.getPath();
                // pattern for "groupId path/(artifactId)/(version)/(filename)"
                // where the filename starts with artifactId-version and is followed by - or .
                //                final Pattern pattern = Pattern.compile( ".*/([^/]+)/([^/]+)/(\\1-\\2[-.][^/]+)$" );
                // NOS-1434 all files with snapshot version(in snapshot folder) should be ignored if !allowsSnapshots
                final Pattern pattern = Pattern.compile( ".*/([^/]+)/([^/]+)/(.[^/]+)$" );
                final Matcher matcher = pattern.matcher( path );
                if ( matcher.find() )
                {
                    String version = matcher.group( 2 );

                    final boolean isSnapshot = SnapshotUtils.isSnapshotVersion( version );
                    if ( ( isSnapshot && !allowsSnapshots ) || ( !isSnapshot && !allowsReleases ) )
                    {
                        logger.debug( "Path {} is filtered out because snapshots/releases disabled" );
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
