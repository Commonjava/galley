package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.LocationExpander;

public class ArtifactManagerImpl
    implements ArtifactManager
{

    @Inject
    private TransferManager transferManager;

    @Inject
    private LocationExpander expander;

    protected ArtifactManagerImpl()
    {
    }

    public ArtifactManagerImpl( final TransferManager transferManager, final LocationExpander expander )
    {
        this.transferManager = transferManager;
        this.expander = expander;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#delete(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public boolean delete( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        final String path = toPath( ref );
        return transferManager.deleteAll( expander.expand( location ), path );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#deleteAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public boolean deleteAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.deleteAll( expander.expand( locations ), toPath( ref ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieve(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Transfer retrieve( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveFirst( expander.expand( location ), toPath( ref ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieveAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveAll( expander.expand( locations ), toPath( ref ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#retrieveFirst(java.util.List, org.commonjava.maven.atlas.ident.ref.ArtifactRef)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveFirst( expander.expand( locations ), toPath( ref ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#store(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef, java.io.InputStream)
     */
    @Override
    public Transfer store( final Location location, final ArtifactRef ref, final InputStream stream )
        throws TransferException
    {
        return transferManager.store( expander.expand( location ), toPath( ref ), stream );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactManager#publish(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ArtifactRef, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final Location location, final ArtifactRef ref, final InputStream stream, final long length )
        throws TransferException
    {
        return transferManager.publish( location, toPath( ref ), stream, length );
    }

    private String toPath( final ArtifactRef ref )
    {
        /* @formatter:off */
        // FIXME: Local snapshot handling...which may also need to be managed in the cache provider...
        return String.format( "%s/%s/%s/%s-%s%s.%s", 
                                  ref.getGroupId().replace('.', '/'), 
                                  ref.getArtifactId(), 
                                  ref.getVersionString(),
                                  ref.getArtifactId(), 
                                  ref.getVersionString(), 
                                  ( ref.getClassifier() == null ? "" : "-" + ref.getClassifier() ), 
                                  ref.getType() );
        /* @formatter:on */
    }

}
