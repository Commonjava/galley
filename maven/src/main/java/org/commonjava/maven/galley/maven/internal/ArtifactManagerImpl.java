package org.commonjava.maven.galley.maven.internal;

import static org.commonjava.maven.galley.maven.util.ArtifactPathUtils.formatArtifactPath;

import java.io.InputStream;
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
import org.commonjava.maven.galley.maven.spi.type.TypeMapper;
import org.commonjava.maven.galley.maven.spi.version.VersionResolver;
import org.commonjava.maven.galley.model.ArtifactBatch;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.VirtualResource;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.util.logging.Logger;

public class ArtifactManagerImpl
    implements ArtifactManager
{
    private final Logger logger = new Logger( getClass() );

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

    public ArtifactManagerImpl( final TransferManager transferManager, final LocationExpander expander, final TypeMapper mapper,
                                final VersionResolver versionResolver )
    {
        this.transferManager = transferManager;
        this.expander = expander;
        this.mapper = mapper;
        this.versionResolver = versionResolver;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#delete(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
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
    @Override
    public boolean deleteAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.deleteAll( new VirtualResource( expander.expand( locations ), formatArtifactPath( ref, mapper ) ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieve(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Transfer retrieve( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveFirst( new VirtualResource( expander.expand( location ), formatArtifactPath( ref, mapper ) ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieveAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public List<Transfer> retrieveAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveAll( new VirtualResource( expander.expand( locations ), formatArtifactPath( ref, mapper ) ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieveFirst(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveFirst( new VirtualResource( expander.expand( locations ), formatArtifactPath( ref, mapper ) ) );
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
    @Override
    public boolean publish( final Location location, final ArtifactRef ref, final InputStream stream, final long length )
        throws TransferException
    {
        return transferManager.publish( new ConcreteResource( location, formatArtifactPath( ref, mapper ) ), stream, length );
    }

    @Override
    public Map<TypeAndClassifier, ConcreteResource> listAvailableArtifacts( final Location location, final ProjectVersionRef ref )
        throws TransferException
    {
        final List<ListingResult> listingResults =
            transferManager.listAll( new VirtualResource( expander.expand( location ), formatArtifactPath( ref.asProjectVersionRef(), mapper ) ) );

        if ( listingResults == null || listingResults.isEmpty() )
        {
            return Collections.emptyMap();
        }

        final Map<TypeAndClassifier, ConcreteResource> result = new LinkedHashMap<TypeAndClassifier, ConcreteResource>();
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
                            logger.info( "Listing found unparsable filename: '%s' from: %s. Skipping", fname, location );
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
    public ProjectVersionRef resolveVariableVersion( final List<? extends Location> locations, final ProjectVersionRef ref )
        throws TransferException
    {
        return versionResolver.resolveVariableVersions( locations, ref );
    }

    @Override
    public ConcreteResource checkExistence( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.findFirstExisting( new VirtualResource( expander.expand( location ), formatArtifactPath( ref, mapper ) ) );
    }

    @Override
    public List<ConcreteResource> findAllExisting( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.findAllExisting( new VirtualResource( expander.expand( locations ), formatArtifactPath( ref, mapper ) ) );
    }

    private void resolveArtifactMappings( final ArtifactBatch batch )
        throws TransferException
    {
        final Map<ArtifactRef, Resource> resources = new HashMap<ArtifactRef, Resource>( batch.size() );
        for ( final ArtifactRef artifact : batch )
        {
            final String path = formatArtifactPath( artifact, mapper );
            final List<? extends Location> locations = expander.expand( batch.getLocations( artifact ) );
            resources.put( artifact, new VirtualResource( locations, path ) );
        }

        batch.setArtifactToResourceMapping( resources );
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

}
