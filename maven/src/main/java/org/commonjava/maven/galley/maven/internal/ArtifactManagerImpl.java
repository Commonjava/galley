/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley.maven.internal;

import static org.commonjava.maven.galley.maven.util.ArtifactPathUtils.formatArtifactPath;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.maven.ArtifactRules;
import org.commonjava.maven.galley.maven.model.ProjectVersionRefLocation;
import org.commonjava.maven.galley.maven.spi.type.TypeMapper;
import org.commonjava.maven.galley.maven.spi.version.VersionResolver;
import org.commonjava.maven.galley.maven.version.LatestVersionSelectionStrategy;
import org.commonjava.maven.galley.model.ArtifactBatch;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.VirtualResource;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactManagerImpl
    implements ArtifactManager
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private TransferManager transferManager;

    @Inject
    private LocationExpander expander;

    @Inject
    private TypeMapper mapper;

    @Inject
    private VersionResolver versionResolver;

    protected ArtifactManagerImpl()
    {
    }

    public ArtifactManagerImpl( final TransferManager transferManager, final LocationExpander expander,
                                final TypeMapper mapper, final VersionResolver versionResolver )
    {
        this.transferManager = transferManager;
        this.expander = expander;
        this.mapper = mapper;
        this.versionResolver = versionResolver;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#delete(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    // TODO: What if the artifact is a snapshot? Do we resolve it???
    // TODO: Metadata repair after deletion
    @Override
    public boolean delete( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        final String path = formatArtifactPath( ref, mapper );
        return transferManager.deleteAll( new VirtualResource( expander.expand( location ), path ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#deleteAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    // TODO: What if the artifact is a snapshot? Do we resolve it and delete all???
    // TODO: Metadata repair after deletion
    @Override
    public boolean deleteAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.deleteAll( new VirtualResource( expander.expand( locations ),
                                                               formatArtifactPath( ref, mapper ) ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieve(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Transfer retrieve( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        final VirtualResource virt = resolveFirstVirtualResource( Collections.singletonList( location ), ref );

        return transferManager.retrieveFirst( virt );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieveAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public List<Transfer> retrieveAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        final VirtualResource virt = resolveAllVirtualResource( locations, ref );
        if ( virt == null )
        {
            return Collections.emptyList();
        }

        return transferManager.retrieveAll( virt );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieveFirst(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        final VirtualResource virt = resolveFirstVirtualResource( locations, ref );

        return transferManager.retrieveFirst( virt );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#store(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef, java.io.InputStream)
     */
    @Override
    public Transfer store( final Location location, final ArtifactRef ref, final InputStream stream )
        throws TransferException
    {
        final List<Location> locations = expander.expand( location );
        final Location selected = ArtifactRules.selectStorageLocation( locations );

        final ConcreteResource resource = new ConcreteResource( selected, formatArtifactPath( ref, mapper ) );
        ArtifactRules.checkStorageAuthorization( resource );
        return selected == null ? null : transferManager.store( resource, stream );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#publish(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef, java.io.InputStream, long)
     */
    // TODO: Snapshot conversion?? Or is that out of scope here?
    @Override
    public boolean publish( final Location location, final ArtifactRef ref, final InputStream stream, final long length )
        throws TransferException
    {
        return transferManager.publish( new ConcreteResource( location, formatArtifactPath( ref, mapper ) ), stream,
                                        length );
    }

    // TODO: This may be incompatible with snapshots, which will have LOTS of entries...
    @Override
    public Map<TypeAndClassifier, ConcreteResource> listAvailableArtifacts( final Location location,
                                                                            final ProjectVersionRef ref )
        throws TransferException
    {
        final List<ListingResult> listingResults =
            transferManager.listAll( new VirtualResource( expander.expand( location ),
                                                          formatArtifactPath( ref.asProjectVersionRef(), mapper ) ) );

        if ( listingResults == null || listingResults.isEmpty() )
        {
            return Collections.emptyMap();
        }

        final Map<TypeAndClassifier, ConcreteResource> result =
            new LinkedHashMap<TypeAndClassifier, ConcreteResource>();
        final String prefix = String.format( "%s-%s", ref.getArtifactId(), ref.getVersionString() );
        for ( final ListingResult listingResult : listingResults )
        {
            //FIXME: snapshot handling.
            for ( final String fname : listingResult.getListing() )
            {
                if ( fname.startsWith( prefix ) )
                {
                    final String remainder = fname.substring( prefix.length() );

                    String classifier = null;
                    String type = null;

                    if ( remainder.startsWith( "-" ) )
                    {
                        // must have a classifier.
                        final int extPos = remainder.indexOf( '.' );
                        if ( extPos < 2 )
                        {
                            logger.warn( "Listing found unparsable filename: '{}' from: {}. Skipping", fname, location );
                            continue;
                        }

                        classifier = remainder.substring( 1, extPos );
                        type = remainder.substring( extPos + 1 );
                    }
                    else if ( remainder.startsWith( "." ) )
                    {
                        type = remainder.substring( 1 );
                    }

                    final ConcreteResource res = (ConcreteResource) listingResult.getResource()
                                                                                 .getChild( fname );
                    result.put( new TypeAndClassifier( type, classifier ), res );
                }
            }
        }

        return result;
    }

    @Override
    public ProjectVersionRef resolveVariableVersion( final Location location, final ProjectVersionRef ref )
        throws TransferException
    {
        return resolveVariableVersion( Collections.singletonList( location ), ref );
    }

    @Override
    public ProjectVersionRef resolveVariableVersion( final List<? extends Location> locations,
                                                     final ProjectVersionRef ref )
        throws TransferException
    {
        return versionResolver.resolveFirstMatchVariableVersion( locations, ref,
                                                                 LatestVersionSelectionStrategy.INSTANCE );
    }

    @Override
    public ConcreteResource checkExistence( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        final VirtualResource virt = resolveAllVirtualResource( Collections.singletonList( location ), ref );
        if ( virt == null )
        {
            return null;
        }

        return transferManager.findFirstExisting( virt );
    }

    @Override
    public List<ConcreteResource> findAllExisting( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.findAllExisting( new VirtualResource( expander.expand( locations ),
                                                                     formatArtifactPath( ref, mapper ) ) );
    }

    @Override
    public ArtifactBatch batchRetrieve( final ArtifactBatch batch )
        throws TransferException
    {
        resolveArtifactMappings( batch );
        return transferManager.batchRetrieve( batch );
    }

    @Override
    public ArtifactBatch batchRetrieveAll( final ArtifactBatch batch )
        throws TransferException
    {
        resolveArtifactMappings( batch );
        return transferManager.batchRetrieveAll( batch );
    }

    private void resolveArtifactMappings( final ArtifactBatch batch )
        throws TransferException
    {
        final Map<ArtifactRef, Resource> resources = new HashMap<ArtifactRef, Resource>( batch.size() );
        for ( final ArtifactRef artifact : batch )
        {
            final VirtualResource virt = resolveFirstVirtualResource( batch.getLocations( artifact ), artifact );
            resources.put( artifact, virt );
        }

        batch.setArtifactToResourceMapping( resources );
    }

    private VirtualResource resolveAllVirtualResource( List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        locations = expander.expand( locations );
        VirtualResource virt = new VirtualResource( locations, formatArtifactPath( ref, mapper ) );

        if ( ref.isVariableVersion() )
        {
            final List<ProjectVersionRefLocation> resolved = resolveAllVariableVersions( locations, ref );
            if ( resolved != null && !resolved.isEmpty() )
            {
                final List<ConcreteResource> resources = new ArrayList<ConcreteResource>( resolved.size() );
                for ( final ProjectVersionRefLocation result : resolved )
                {
                    resources.add( new ConcreteResource( result.getLocation(), formatArtifactPath( ref, mapper ) ) );
                }

                virt = new VirtualResource( resources );
            }
            else
            {
                return null;
            }
        }

        return virt;
    }

    private VirtualResource resolveFirstVirtualResource( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException
    {
        locations = expander.expand( locations );
        if ( ref.isVariableVersion() )
        {
            final ProjectVersionRefLocation resolved = resolveSingleVariableVersion( locations, ref );
            if ( resolved != null )
            {
                locations = Collections.singletonList( resolved.getLocation() );
                ref = (ArtifactRef) resolved.getRef();
            }
        }

        return new VirtualResource( expander.expand( locations ), formatArtifactPath( ref, mapper ) );
    }

    private ProjectVersionRefLocation resolveSingleVariableVersion( final List<? extends Location> locations,
                                                                    final ArtifactRef ref )
        throws TransferException
    {
        return versionResolver.resolveFirstMatchVariableVersionLocation( locations, ref,
                                                                         LatestVersionSelectionStrategy.INSTANCE );
    }

    private List<ProjectVersionRefLocation> resolveAllVariableVersions( final List<? extends Location> locations,
                                                                        final ArtifactRef ref )
        throws TransferException
    {
        return versionResolver.resolveAllVariableVersionLocations( locations, ref,
                                                                   LatestVersionSelectionStrategy.INSTANCE );
    }

}
