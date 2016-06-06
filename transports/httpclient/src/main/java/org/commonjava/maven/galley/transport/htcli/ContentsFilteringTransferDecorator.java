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
package org.commonjava.maven.galley.transport.htcli;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.commonjava.maven.atlas.ident.util.SnapshotUtils;
import org.commonjava.maven.atlas.ident.version.part.SnapshotPart;
import org.commonjava.maven.galley.io.AbstractTransferDecorator;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.OverriddenBooleanValue;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a decorator responsible for filtering out location contents based on location settings. Effectively it is
 * designed to filter out snapshot or release versions from proxied remote repositories when configured to not
 * provide the snapshot or release versions.
 *
 * @author pkocandr
 */
@Named( "contents-filtering-galley-decorator" )
@Alternative
public class ContentsFilteringTransferDecorator
extends AbstractTransferDecorator
{

    public ContentsFilteringTransferDecorator()
    {
    }

    public ContentsFilteringTransferDecorator( final TransferDecorator next )
    {
        super( next );
    }

    @Override
    protected OverriddenBooleanValue decorateExistsInternal( final Transfer transfer )
    {
        final Location loc = transfer.getLocation();
        final boolean allowsSnapshots = loc.allowsSnapshots();
        final boolean allowsReleases = loc.allowsReleases();
        if ( loc instanceof HttpLocation && ( !allowsSnapshots || !allowsReleases ) )
        {
            if ( transfer.isFile() )
            {
                final String path = transfer.getPath();
                // pattern for "groupId path/(artifactId)/(version)/(filename)"
                // where the filename starts with artifactId-version and is followed by - or .
                final Pattern pattern = Pattern.compile( ".*/([^/]+)/([^/]+)/(\\1-\\2[-.][^/]+)$" );
                final Matcher matcher = pattern.matcher( path );
                if ( matcher.find() )
                {
                    String version = matcher.group( 2 );

                    final boolean isSnapshot = SnapshotUtils.isSnapshotVersion( version );
                    if ( isSnapshot && !allowsSnapshots || !isSnapshot && !allowsReleases )
                    {
                        return OverriddenBooleanValue.OVERRIDE_FALSE;
                    }
                }
            }
        }

        return OverriddenBooleanValue.DEFER;
    }

    @Override
    protected OutputStream decorateWriteInternal( final OutputStream stream, final Transfer transfer, final TransferOperation op )
            throws IOException
    {
        final Location loc = transfer.getLocation();
        final boolean allowsSnapshots = loc.allowsSnapshots();
        final boolean allowsReleases = loc.allowsReleases();
        if ( loc instanceof HttpLocation && ( !allowsSnapshots || !allowsReleases )
                && transfer.getFullPath().endsWith( "maven-metadata.xml" ) )
        {
            return new MetadataFilteringOutputStream( stream, allowsSnapshots, allowsReleases, transfer );
        }
        else
        {
            return stream;
        }
    }

    /**
     * Alters the listing to filter out artifacts belonging to a version that
     * should not be provided via the proxy.
     */
    @Override
    protected String[] decorateListingInternal( final Transfer transfer, final String[] listing )
            throws IOException
    {
        final Location loc = transfer.getLocation();
        final boolean allowsSnapshots = loc.allowsSnapshots();
        final boolean allowsReleases = loc.allowsReleases();
        // process only proxied locations, i.e. HttpLocation instances
        if ( loc instanceof HttpLocation && ( !allowsSnapshots || !allowsReleases ) )
        {
            final String[] pathElements = transfer.getPath().split( "/" );
            // process only paths that *can* be a GAV
            if ( pathElements.length >= 3 )
            {
                final String artifactId = pathElements[ pathElements.length - 2 ];
                final String version = pathElements[ pathElements.length - 1 ];
                final boolean snapshotVersion = SnapshotUtils.isSnapshotVersion( version );
                if ( !allowsSnapshots && snapshotVersion || !allowsReleases && !snapshotVersion )
                {
                    final List<String> result = new ArrayList<String>( listing.length );
                    for ( final String element : listing )
                    {
                        // do not include artifacts in the list
                        if ( !isArtifact( element, artifactId, version ) )
                        {
                            result.add( element );
                        }
                    }
                    return result.toArray( new String[ result.size() ] );
                }
            }
        }
        return listing;
    }

    /**
     * Checks if the given element is an artifact. Artifacts always starts with
     * &lt;artifactId&gt;-&lt;version&gt;.
     */
    private boolean isArtifact( final String element, final String artifactId,
            final String version )
    {
        if ( element.endsWith( "/" ) )
        {
            return false;
        }

        boolean isRemoteSnapshot = false;
        if ( SnapshotUtils.isSnapshotVersion( version )
                && element.startsWith( artifactId + '-' )
                && !element.startsWith( artifactId + '-' + version ) )
        {
            final SnapshotPart snapshotPart = SnapshotUtils.extractSnapshotVersionPart( version );
            final int artIdLenght = artifactId.length() + 1 + version.length() - snapshotPart.getLiteral().length() + 1;
            isRemoteSnapshot = SnapshotUtils.isRemoteSnapshotVersionPart(
                    StringUtils.substring( element, artIdLenght, artIdLenght + 17 ) ) ;
        }

        return element.startsWith( artifactId + '-' + version + '-' )
                || element.startsWith( artifactId + '-' + version + '.' )
                || isRemoteSnapshot;
    }


    private static class MetadataFilteringOutputStream extends FilterOutputStream
    {

        private static final String LATEST = "<latest>([^<]+)</latest>";

        private static final String RELEASE = "<release>([^<]+)</release>";

        private static final String VERSION = "<version>([^<]+)</version>";

        private static final String VERSIONS = "<versions>[\\s]*(?:(" + VERSION + ")[\\s]*)+</versions>";

        private StringBuilder buffer = new StringBuilder();

        private final boolean allowsSnapshots;

        private final boolean allowsReleases;

        private Transfer transfer;

        private MetadataFilteringOutputStream( final OutputStream stream, final boolean allowsSnapshots,
                                               final boolean allowsReleases, Transfer transfer )
        {
            super( stream );
            this.allowsSnapshots = allowsSnapshots;
            this.allowsReleases = allowsReleases;
            this.transfer = transfer;
        }

        private String filterMetadata()
        {
            if ( buffer.length() == 0 )
            {
                return "";
            }

            // filter versions from GA metadata
            final Pattern versionsPattern = Pattern.compile( VERSIONS, Pattern.MULTILINE );
            final Matcher m = versionsPattern.matcher( buffer );
            final List<String> versions = new ArrayList<String>();
            if ( m.find() )
            {
                final Pattern versionPattern = Pattern.compile( VERSION );
                final Matcher versionMatcher = versionPattern.matcher( m.group() );
                while ( versionMatcher.find() )
                {
                    versions.add( versionMatcher.group( 1 ) );
                }
            }

            boolean changed = false;
            for ( final String version : new ArrayList<String>( versions ) )
            {
                final boolean isSnapshot = SnapshotUtils.isSnapshotVersion( version );
                if ( !allowsSnapshots && isSnapshot || !allowsReleases && !isSnapshot )
                {
                    Logger logger = LoggerFactory.getLogger( getClass() );
                    logger.debug( "FILTER: Removing prohibited version: {} from: {}", version, transfer );
                    versions.remove( version );
                    changed = true;
                }
            }

            String filteredMetadata = buffer.toString();
            if ( changed )
            {
                String filteredVersions;
                if ( versions.size() == 0 )
                {
                    filteredVersions = "<versions></versions>";
                }
                else
                {
                    filteredVersions = "<versions>\n<version>" + StringUtils.join( versions, "</version>\n<version>" ) + "</version>\n</versions>";
                }
                filteredMetadata = filteredMetadata.replaceFirst( VERSIONS, filteredVersions );
            }

            final Pattern latestPattern = Pattern.compile( LATEST );
            final Matcher latestMatcher = latestPattern.matcher( filteredMetadata );
            if ( latestMatcher.find() )
            {
                final String latestVersion = latestMatcher.group( 1 );
                final boolean isSnapshot = latestVersion.endsWith( "-SNAPSHOT" );
                if ( !allowsSnapshots && isSnapshot || !allowsReleases && !isSnapshot )
                {
                    Logger logger = LoggerFactory.getLogger( getClass() );
                    logger.debug( "FILTER: Recalculating LATEST version; supplied value is prohibited: {} from: {}", latestVersion, transfer );

                    String newLatest;
                    if ( versions.size() > 0 )
                    {
                        newLatest = "<latest>" + versions.get( versions.size() - 1 ) + "</latest>";
                    }
                    else
                    {
                        newLatest = "<latest></latest>";
                    }
                    filteredMetadata = filteredMetadata.replaceFirst( LATEST, newLatest );
                }
            }

            if ( !allowsReleases )
            {
                final Pattern releasePattern = Pattern.compile( RELEASE );
                final Matcher releaseMatcher = releasePattern.matcher( filteredMetadata );
                if ( releaseMatcher.find() )
                {
                    Logger logger = LoggerFactory.getLogger( getClass() );
                    logger.debug( "FILTER: Suppressing prohibited release fields from: {}", transfer );

                    filteredMetadata = filteredMetadata.replaceFirst( RELEASE, "<release></release>" );
                }
            }

            // filter snapshots from GAV metadata
            if ( !allowsSnapshots )
            {
                Logger logger = LoggerFactory.getLogger( getClass() );
                logger.debug( "FILTER: Suppressing prohibited snapshot fields from: {}", transfer );

                final String snapshots = StringUtils.substringBetween( filteredMetadata,
                                                                       "<snapshotVersions>",
                                                                       "</snapshotVersions>" );
                if ( snapshots != null )
                {
                    filteredMetadata = filteredMetadata.replace( snapshots, "" );
                }

                final String snapshot = StringUtils.substringBetween( filteredMetadata, "<snapshot>", "</snapshot>" );
                if ( snapshot != null )
                {
                    filteredMetadata = filteredMetadata.replace( snapshot, "" );
                }
            }

            return filteredMetadata;
        }

        @Override
        public void write( final int b ) throws IOException
        {
            buffer.append( (char) b );
        }

        @Override
        public void flush() throws IOException {
            try
            {
                out.write( filterMetadata().getBytes() );
                out.flush();
            }
            finally
            {
                buffer = null;
            }
        }
    }
}
