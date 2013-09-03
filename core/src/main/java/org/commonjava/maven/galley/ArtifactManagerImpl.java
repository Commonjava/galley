package org.commonjava.maven.galley;

import static org.commonjava.maven.galley.util.ArtifactFormatUtils.formatArtifactPath;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.type.TypeMapper;
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
    private ArtifactMetadataManager metadataManager;

    protected ArtifactManagerImpl()
    {
    }

    public ArtifactManagerImpl( final TransferManager transferManager, final LocationExpander expander, final TypeMapper mapper )
    {
        this.transferManager = transferManager;
        this.expander = expander;
        this.mapper = mapper;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#delete(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public boolean delete( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        final String path = formatArtifactPath( ref, mapper );
        return transferManager.deleteAll( expander.expand( location ), path );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#deleteAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public boolean deleteAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.deleteAll( expander.expand( locations ), formatArtifactPath( ref, mapper ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieve(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Transfer retrieve( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveFirst( expander.expand( location ), formatArtifactPath( ref, mapper ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieveAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public List<Transfer> retrieveAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveAll( expander.expand( locations ), formatArtifactPath( ref, mapper ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieveFirst(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveFirst( expander.expand( locations ), formatArtifactPath( ref, mapper ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#store(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef, java.io.InputStream)
     */
    @Override
    public Transfer store( final Location location, final ArtifactRef ref, final InputStream stream )
        throws TransferException
    {
        return transferManager.store( expander.expand( location ), formatArtifactPath( ref, mapper ), stream );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#publish(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final Location location, final ArtifactRef ref, final InputStream stream, final long length )
        throws TransferException
    {
        return transferManager.publish( new Resource( location, formatArtifactPath( ref, mapper ) ), stream, length );
    }

    @Override
    public TypeAndClassifier[] listAvailableArtifacts( final Location location, final ProjectVersionRef ref )
        throws TransferException
    {
        final List<ListingResult> listingResults =
            transferManager.listAll( expander.expand( location ), formatArtifactPath( ref.asProjectVersionRef(), mapper ) );

        if ( listingResults == null || listingResults.isEmpty() )
        {
            return new TypeAndClassifier[0];
        }

        final String prefix = String.format( "%s-%s", ref.getArtifactId(), ref.getVersionString() );
        final Set<TypeAndClassifier> artifacts = new HashSet<>();
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

                    artifacts.add( new TypeAndClassifier( type, classifier ) );
                }
            }
        }

        return artifacts.toArray( new TypeAndClassifier[artifacts.size()] );
    }

    @Override
    public ProjectVersionRef resolveSnapshotVersion( final Location location, final ProjectVersionRef ref )
        throws TransferException
    {
        if ( !ref.isSpecificVersion() )
        {
            throw new TransferException( "Cannot resolve the snapshot in a compound or range version specification!" );
        }

        if ( ref.isRelease() )
        {
            return ref;
        }

        final Transfer metadata = metadataManager.retrieve( location, ref );

        return null;
    }

}
