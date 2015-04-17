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
package org.commonjava.maven.galley.maven.internal.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.util.JoinString;
import org.commonjava.maven.atlas.ident.util.VersionUtils;
import org.commonjava.maven.atlas.ident.version.InvalidVersionSpecificationException;
import org.commonjava.maven.atlas.ident.version.SingleVersion;
import org.commonjava.maven.atlas.ident.version.VersionSpec;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.ProjectVersionRefLocation;
import org.commonjava.maven.galley.maven.model.view.meta.MavenMetadataView;
import org.commonjava.maven.galley.maven.parse.MavenMetadataReader;
import org.commonjava.maven.galley.maven.spi.version.VersionResolver;
import org.commonjava.maven.galley.maven.version.LatestVersionSelectionStrategy;
import org.commonjava.maven.galley.maven.version.VersionSelectionStrategy;
import org.commonjava.maven.galley.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VersionResolverImpl
    implements VersionResolver
{

    private static final String SNAP_VERSION_XPATH = "/metadata/versioning/snapshotVersions/snapshotVersion[1]/value";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private MavenMetadataReader metadataReader;

    protected VersionResolverImpl()
    {
    }

    public VersionResolverImpl( final MavenMetadataReader metadataReader )
    {
        this.metadataReader = metadataReader;
    }

    @Override
    public ProjectVersionRef resolveVariableVersions( final List<? extends Location> locations,
                                                      final ProjectVersionRef ref )
        throws TransferException
    {
        return resolveFirstMatchVariableVersion( locations, ref, LatestVersionSelectionStrategy.INSTANCE );
    }

    @Override
    public ProjectVersionRef resolveLatestVariableVersion( final List<? extends Location> locations,
                                                           final ProjectVersionRef ref,
                                                           final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        if ( !ref.getVersionSpec()
                 .isSingle() )
        {
            return resolveLatestMultiRef( locations, ref, selectionStrategy );
        }
        else if ( ref.isSnapshot() )
        {
            return resolveLatestSnapshotRef( locations, ref, selectionStrategy );
        }
        else
        {
            return ref;
        }
    }

    @Override
    public ProjectVersionRef resolveFirstMatchVariableVersion( final List<? extends Location> locations,
                                                               final ProjectVersionRef ref,
                                                               final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        if ( !ref.getVersionSpec()
                 .isSingle() )
        {
            return resolveFirstMultiRef( locations, ref, selectionStrategy );
        }
        else if ( ref.isSnapshot() )
        {
            return resolveFirstSnapshotRef( locations, ref, selectionStrategy );
        }
        else
        {
            return ref;
        }
    }

    @Override
    public ProjectVersionRefLocation resolveLatestVariableVersionLocation( final List<? extends Location> locations,
                                                                           final ProjectVersionRef ref,
                                                                           final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        if ( !ref.getVersionSpec()
                 .isSingle() )
        {
            return resolveLatestMultiRefWithLocation( locations, ref, selectionStrategy );
        }
        else if ( ref.isSnapshot() )
        {
            return resolveLatestSnapshotRefWithLocation( locations, ref, selectionStrategy );
        }

        return null;
    }

    @Override
    public ProjectVersionRefLocation resolveFirstMatchVariableVersionLocation( final List<? extends Location> locations,
                                                                               final ProjectVersionRef ref,
                                                                               final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        if ( !ref.getVersionSpec()
                 .isSingle() )
        {
            return resolveFirstMultiRefWithLocation( locations, ref, selectionStrategy );
        }
        else if ( ref.isSnapshot() )
        {
            return resolveFirstSnapshotRefWithLocation( locations, ref, selectionStrategy );
        }

        return null;
    }

    @Override
    public List<ProjectVersionRefLocation> resolveAllVariableVersionLocations( final List<? extends Location> locations,
                                                                               final ArtifactRef ref,
                                                                               final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        if ( !ref.getVersionSpec()
                 .isSingle() )
        {
            return resolveAllMultiRefsWithLocations( locations, ref, selectionStrategy );
        }
        else if ( ref.isSnapshot() )
        {
            return resolveAllSnapshotRefsWithLocations( locations, ref, selectionStrategy );
        }

        return Collections.emptyList();
    }

    private ProjectVersionRef resolveLatestSnapshotRef( final List<? extends Location> locations,
                                                        final ProjectVersionRef ref,
                                                        final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        final ProjectVersionRefLocation result =
            resolveLatestSnapshotRefWithLocation( locations, ref, selectionStrategy );

        return result == null ? null : result.getRef();
    }

    private ProjectVersionRefLocation resolveLatestSnapshotRefWithLocation( final List<? extends Location> locations,
                                                                            final ProjectVersionRef ref,
                                                                            final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        final Map<SingleVersion, Location> available = new TreeMap<SingleVersion, Location>();
        for ( final Location location : locations )
        {
            try
            {
                final MavenMetadataView metadata =
                    metadataReader.getMetadata( ref, Collections.singletonList( location ) );

                if ( metadata != null )
                {
                    addSnapshotFrom( metadata, location, ref, available );
                }
            }
            catch ( final GalleyMavenException e )
            {
                debug( "Failed to resolve/parse metadata for snapshot version of: %s from: %s.", e, ref, location );
            }
        }

        if ( available.isEmpty() )
        {
            return null;
        }

        final List<SingleVersion> versions = new ArrayList<SingleVersion>( available.keySet() );
        Collections.sort( versions );

        final SingleVersion selected = selectionStrategy.select( versions );
        if ( selected == null )
        {
            return null;
        }

        return new ProjectVersionRefLocation( ref.selectVersion( selected ), available.get( selected ) );
    }

    private ProjectVersionRef resolveFirstSnapshotRef( final List<? extends Location> locations,
                                                       final ProjectVersionRef ref,
                                                       final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        final ProjectVersionRefLocation result =
            resolveFirstSnapshotRefWithLocation( locations, ref, selectionStrategy );

        return result == null ? null : result.getRef();
    }

    private ProjectVersionRefLocation resolveFirstSnapshotRefWithLocation( final List<? extends Location> locations,
                                                                           final ProjectVersionRef ref,
                                                                           final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        nextLoc: for ( final Location location : locations )
        {
            final Map<SingleVersion, Location> available = new TreeMap<SingleVersion, Location>();
            try
            {
                final MavenMetadataView metadata =
                    metadataReader.getMetadata( ref, Collections.singletonList( location ) );

                if ( metadata != null )
                {
                    addSnapshotFrom( metadata, location, ref, available );
                }
            }
            catch ( final GalleyMavenException e )
            {
                debug( "Failed to resolve/parse metadata for snapshot version of: %s from: %s.", e, ref, location );
            }

            if ( !available.isEmpty() )
            {
                final List<SingleVersion> versions = new ArrayList<SingleVersion>( available.keySet() );
                Collections.sort( versions );

                final SingleVersion selected = selectionStrategy.select( versions );
                if ( selected == null )
                {
                    continue nextLoc;
                }

                return new ProjectVersionRefLocation( ref.selectVersion( selected ), available.get( selected ) );
            }
        }

        return null;
    }

    private void addSnapshotFrom( final MavenMetadataView metadata, final Location location,
                                  final ProjectVersionRef ref, final Map<SingleVersion, Location> available )
        throws GalleyMavenException
    {
        final String version = metadata.resolveSingleValue( SNAP_VERSION_XPATH );
        logger.debug( "Latest snapshot version in metadata is: {}", version );

        if ( version != null )
        {
            try
            {
                final SingleVersion ver = VersionUtils.createSingleVersion( version );
                if ( !available.containsKey( ver ) )
                {
                    logger.debug( "Found candidate snapshot: {}", ver );
                    available.put( ver, location );
                }
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                debug( "Unparsable version spec found in metadata: '%s' for: %s from: %s", e, version, ref, location );
            }
        }
    }

    private ProjectVersionRef resolveLatestMultiRef( final List<? extends Location> locations,
                                                     final ProjectVersionRef ref,
                                                     final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        final ProjectVersionRefLocation result = resolveLatestMultiRefWithLocation( locations, ref, selectionStrategy );
        return result == null ? null : result.getRef();
    }

    private ProjectVersionRefLocation resolveLatestMultiRefWithLocation( final List<? extends Location> locations,
                                                                         final ProjectVersionRef ref,
                                                                         final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        final Map<SingleVersion, Location> available = new TreeMap<SingleVersion, Location>();
        for ( final Location location : locations )
        {
            try
            {
                final MavenMetadataView metadata =
                    metadataReader.getMetadata( ref.asProjectRef(), Collections.singletonList( location ) );

                if ( metadata != null )
                {
                    final List<String> versions = metadata.resolveValues( "/metadata/versioning/versions/version" );

                    if ( versions != null )
                    {
                        for ( final String version : versions )
                        {
                            try
                            {
                                final SingleVersion spec = VersionUtils.createSingleVersion( version );
                                if ( !available.containsKey( spec ) )
                                {
                                    available.put( spec, location );
                                }
                            }
                            catch ( final InvalidVersionSpecificationException e )
                            {
                                debug( "Unparsable version spec found in metadata: '%s' for: %s from: %s.", e, version,
                                       ref, location );
                            }
                        }
                    }
                }
            }
            catch ( final GalleyMavenException e )
            {
                debug( "Failed to resolve/parse metadata for variable version of: '%s' from: %s.", e, ref, location );
            }
        }

        if ( !available.isEmpty() )
        {
            final VersionSpec spec = ref.getVersionSpec();

            final List<SingleVersion> versions = new ArrayList<SingleVersion>( available.keySet() );
            Collections.sort( versions );
            while ( !versions.isEmpty() )
            {
                final SingleVersion selected = selectionStrategy.select( versions );
                if ( selected == null )
                {
                    return null;
                }

                versions.remove( selected );
                if ( selected.isConcrete() && spec.contains( selected ) )
                {
                    return new ProjectVersionRefLocation( ref.selectVersion( selected ), available.get( selected ) );
                }
            }
        }

        return null;
    }

    private ProjectVersionRef resolveFirstMultiRef( final List<? extends Location> locations,
                                                    final ProjectVersionRef ref,
                                                    final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        final ProjectVersionRefLocation result = resolveFirstMultiRefWithLocation( locations, ref, selectionStrategy );
        return result == null ? null : result.getRef();
    }

    private ProjectVersionRefLocation resolveFirstMultiRefWithLocation( final List<? extends Location> locations,
                                                                        final ProjectVersionRef ref,
                                                                        final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        nextLoc: for ( final Location location : locations )
        {
            final Map<SingleVersion, Location> available = new TreeMap<SingleVersion, Location>();
            try
            {
                final MavenMetadataView metadata =
                    metadataReader.getMetadata( ref.asProjectRef(), Collections.singletonList( location ) );

                if ( metadata != null )
                {
                    final List<String> versions = metadata.resolveValues( "/metadata/versioning/versions/version" );

                    if ( versions != null )
                    {
                        for ( final String version : versions )
                        {
                            try
                            {
                                final SingleVersion spec = VersionUtils.createSingleVersion( version );
                                if ( !available.containsKey( spec ) )
                                {
                                    available.put( spec, location );
                                }
                            }
                            catch ( final InvalidVersionSpecificationException e )
                            {
                                debug( "Unparsable version spec found in metadata: '%s' for: %s from: %s.", e, version,
                                       ref, location );
                            }
                        }
                    }
                }
            }
            catch ( final GalleyMavenException e )
            {
                debug( "Failed to resolve/parse metadata for variable version of: '%s' from: %s.", e, ref, location );
            }

            if ( !available.isEmpty() )
            {
                final VersionSpec spec = ref.getVersionSpec();

                final List<SingleVersion> versions = new ArrayList<SingleVersion>( available.keySet() );
                Collections.sort( versions );
                while ( !versions.isEmpty() )
                {
                    final SingleVersion selected = selectionStrategy.select( versions );
                    if ( selected == null )
                    {
                        continue nextLoc;
                    }

                    versions.remove( selected );
                    if ( selected.isConcrete() && spec.contains( selected ) )
                    {
                        return new ProjectVersionRefLocation( ref.selectVersion( selected ), available.get( selected ) );
                    }
                }
            }
        }

        return null;
    }

    private void debug( final String message, final Throwable e, final Object... params )
    {
        final String format = message.replaceAll( "\\{\\}", "%s" ) + "\n%s\n  %s";
        final Object[] p = new Object[params.length + 2];
        System.arraycopy( params, 0, p, 0, params.length );
        p[p.length - 2] = e.getMessage();
        p[p.length - 1] = new JoinString( "\n  ", e.getStackTrace() );

        logger.debug( "{}", new Object()
        {
            @Override
            public String toString()
            {
                return String.format( format, p );
            }
        } );
    }

    private List<ProjectVersionRefLocation> resolveAllMultiRefsWithLocations( final List<? extends Location> locations,
                                                                              final ProjectVersionRef ref,
                                                                              final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        final Map<SingleVersion, Location> available = new TreeMap<SingleVersion, Location>();
        for ( final Location location : locations )
        {
            try
            {
                final MavenMetadataView metadata =
                    metadataReader.getMetadata( ref.asProjectRef(), Collections.singletonList( location ) );

                if ( metadata != null )
                {
                    final List<String> versions = metadata.resolveValues( "/metadata/versioning/versions/version" );

                    if ( versions != null )
                    {
                        for ( final String version : versions )
                        {
                            try
                            {
                                final SingleVersion spec = VersionUtils.createSingleVersion( version );
                                if ( !available.containsKey( spec ) )
                                {
                                    available.put( spec, location );
                                }
                            }
                            catch ( final InvalidVersionSpecificationException e )
                            {
                                debug( "Unparsable version spec found in metadata: '%s' for: %s from: %s.", e, version,
                                       ref, location );
                            }
                        }
                    }
                }
            }
            catch ( final GalleyMavenException e )
            {
                debug( "Failed to resolve/parse metadata for variable version of: '%s' from: %s.", e, ref, location );
            }
        }

        if ( !available.isEmpty() )
        {
            final List<ProjectVersionRefLocation> result = new ArrayList<ProjectVersionRefLocation>();

            final VersionSpec spec = ref.getVersionSpec();

            final List<SingleVersion> versions = new ArrayList<SingleVersion>( available.keySet() );
            Collections.sort( versions );
            while ( !versions.isEmpty() )
            {
                final SingleVersion selected = selectionStrategy.select( versions );
                if ( selected != null )
                {
                    versions.remove( selected );
                    if ( selected.isConcrete() && spec.contains( selected ) )
                    {
                        result.add( new ProjectVersionRefLocation( ref.selectVersion( selected ),
                                                                   available.get( selected ) ) );
                    }
                }
            }

            return result;
        }

        return Collections.<ProjectVersionRefLocation> emptyList();
    }

    private List<ProjectVersionRefLocation> resolveAllSnapshotRefsWithLocations( final List<? extends Location> locations,
                                                                                 final ProjectVersionRef ref,
                                                                                 final VersionSelectionStrategy selectionStrategy )
        throws TransferException
    {
        final Map<SingleVersion, Location> available = new TreeMap<SingleVersion, Location>();
        for ( final Location location : locations )
        {
            try
            {
                final MavenMetadataView metadata =
                    metadataReader.getMetadata( ref, Collections.singletonList( location ) );

                if ( metadata != null )
                {
                    final String latest = metadata.resolveSingleValue( "/metadata/versioning/latest" );
                    if ( latest != null )
                    {
                        try
                        {
                            final SingleVersion ver = VersionUtils.createSingleVersion( latest );
                            if ( ver.isSnapshot() )
                            {
                                if ( !available.containsKey( ver ) )
                                {
                                    available.put( ver, location );
                                }
                            }
                        }
                        catch ( final InvalidVersionSpecificationException e )
                        {
                            debug( "Unparsable version spec found in metadata: '%s' for: %s from: %s", e, latest, ref,
                                   location );
                        }
                    }
                }
            }
            catch ( final GalleyMavenException e )
            {
                debug( "Failed to resolve/parse metadata for snapshot version of: %s from: %s.", e, ref, location );
            }
        }

        if ( !available.isEmpty() )
        {
            return Collections.emptyList();
        }

        final List<SingleVersion> versions = new ArrayList<SingleVersion>( available.keySet() );
        Collections.sort( versions );

        final List<ProjectVersionRefLocation> result = new ArrayList<ProjectVersionRefLocation>();

        while ( !versions.isEmpty() )
        {
            final SingleVersion selected = selectionStrategy.select( versions );
            if ( selected != null )
            {
                versions.remove( selected );
                result.add( new ProjectVersionRefLocation( ref.selectVersion( selected ), available.get( selected ) ) );
            }
        }

        return result;
    }

}
