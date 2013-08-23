package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.LocationExpander;

public class ArtifactMetadataManagerImpl
    implements ArtifactMetadataManager
{

    @Inject
    private TransferManager transferManager;

    @Inject
    private LocationExpander expander;

    protected ArtifactMetadataManagerImpl()
    {
    }

    public ArtifactMetadataManagerImpl( final TransferManager transferManager, final LocationExpander expander )
    {
        this.transferManager = transferManager;
        this.expander = expander;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#delete(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ProjectRef)
     */
    @Override
    public boolean delete( final Location location, final ProjectRef ref )
        throws TransferException
    {
        return delete( location, ref, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#delete(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.lang.String)
     */
    @Override
    public boolean delete( final Location location, final ProjectRef ref, final String filename )
        throws TransferException
    {
        final String path = toPath( ref, filename );
        return transferManager.deleteAll( expander.expand( location ), path );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#delete(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public boolean delete( final Location location, final String groupId )
        throws TransferException
    {
        return delete( location, groupId, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#delete(org.commonjava.maven.galley.model.Location, java.lang.String, java.lang.String)
     */
    @Override
    public boolean delete( final Location location, final String groupId, final String filename )
        throws TransferException
    {
        final String path = toPath( groupId, filename );
        return transferManager.deleteAll( expander.expand( location ), path );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#deleteAll(java.util.List, java.lang.String)
     */
    @Override
    public boolean deleteAll( final List<? extends Location> locations, final String groupId )
        throws TransferException
    {
        return deleteAll( locations, groupId, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#deleteAll(java.util.List, java.lang.String, java.lang.String)
     */
    @Override
    public boolean deleteAll( final List<? extends Location> locations, final String groupId, final String filename )
        throws TransferException
    {
        return transferManager.deleteAll( expander.expand( locations ), toPath( groupId, filename ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#deleteAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ProjectRef)
     */
    @Override
    public boolean deleteAll( final List<? extends Location> locations, final ProjectRef ref )
        throws TransferException
    {
        return deleteAll( locations, ref, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#deleteAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.lang.String)
     */
    @Override
    public boolean deleteAll( final List<? extends Location> locations, final ProjectRef ref, final String filename )
        throws TransferException
    {
        return transferManager.deleteAll( expander.expand( locations ), toPath( ref, filename ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieve(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public Transfer retrieve( final Location location, final String groupId )
        throws TransferException
    {
        return retrieve( location, groupId, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieve(org.commonjava.maven.galley.model.Location, java.lang.String, java.lang.String)
     */
    @Override
    public Transfer retrieve( final Location location, final String groupId, final String filename )
        throws TransferException
    {
        return transferManager.retrieveFirst( expander.expand( location ), toPath( groupId, filename ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieve(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ProjectRef)
     */
    @Override
    public Transfer retrieve( final Location location, final ProjectRef ref )
        throws TransferException
    {
        return retrieve( location, ref, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieve(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.lang.String)
     */
    @Override
    public Transfer retrieve( final Location location, final ProjectRef ref, final String filename )
        throws TransferException
    {
        return transferManager.retrieveFirst( expander.expand( location ), toPath( ref, filename ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieveAll(java.util.List, java.lang.String)
     */
    @Override
    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final String groupId )
        throws TransferException
    {
        return retrieveAll( locations, groupId, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieveAll(java.util.List, java.lang.String, java.lang.String)
     */
    @Override
    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final String groupId,
                                      final String filename )
        throws TransferException
    {
        return transferManager.retrieveAll( expander.expand( locations ), toPath( groupId, filename ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieveAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ProjectRef)
     */
    @Override
    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final ProjectRef ref )
        throws TransferException
    {
        return retrieveAll( locations, ref, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieveAll(java.util.List, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.lang.String)
     */
    @Override
    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final ProjectRef ref,
                                      final String filename )
        throws TransferException
    {
        return transferManager.retrieveAll( expander.expand( locations ), toPath( ref, filename ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieveFirst(java.util.List, java.lang.String)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> locations, final String groupId )
        throws TransferException
    {
        return retrieveFirst( locations, groupId, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieveFirst(java.util.List, java.lang.String, java.lang.String)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> locations, final String groupId, final String filename )
        throws TransferException
    {
        return transferManager.retrieveFirst( expander.expand( locations ), toPath( groupId, filename ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieveFirst(java.util.List, org.commonjava.maven.atlas.ident.ref.ProjectRef)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> locations, final ProjectRef ref )
        throws TransferException
    {
        return retrieveFirst( locations, ref, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#retrieveFirst(java.util.List, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.lang.String)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> locations, final ProjectRef ref, final String filename )
        throws TransferException
    {
        return transferManager.retrieveFirst( expander.expand( locations ), toPath( ref, filename ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#store(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream)
     */
    @Override
    public Transfer store( final Location location, final String groupId, final InputStream stream )
        throws TransferException
    {
        return store( location, groupId, null, stream );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#store(org.commonjava.maven.galley.model.Location, java.lang.String, java.lang.String, java.io.InputStream)
     */
    @Override
    public Transfer store( final Location location, final String groupId, final String filename,
                           final InputStream stream )
        throws TransferException
    {
        return transferManager.store( expander.expand( location ), toPath( groupId, filename ), stream );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#store(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.io.InputStream)
     */
    @Override
    public Transfer store( final Location location, final ProjectRef ref, final InputStream stream )
        throws TransferException
    {
        return store( location, ref, null, stream );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#store(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.lang.String, java.io.InputStream)
     */
    @Override
    public Transfer store( final Location location, final ProjectRef ref, final String filename,
                           final InputStream stream )
        throws TransferException
    {
        return transferManager.store( expander.expand( location ), toPath( ref, filename ), stream );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final Location location, final String groupId, final InputStream stream, final long length )
        throws TransferException
    {
        return publish( location, groupId, null, stream, length, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.lang.String, java.io.InputStream, long, java.lang.String)
     */
    @Override
    public boolean publish( final Location location, final String groupId, final String filename,
                            final InputStream stream, final long length, final String contentType )
        throws TransferException
    {
        return transferManager.publish( new Resource( location, toPath( groupId, filename ) ), stream, length,
                                        contentType );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#publish(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final Location location, final ProjectRef ref, final InputStream stream, final long length )
        throws TransferException
    {
        return publish( location, ref, null, stream, length, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.ArtifactMetadataManager#publish(org.commonjava.maven.galley.model.Location, org.commonjava.maven.atlas.ident.ref.ProjectRef, java.lang.String, java.io.InputStream, long, java.lang.String)
     */
    @Override
    public boolean publish( final Location location, final ProjectRef ref, final String filename,
                            final InputStream stream, final long length, final String contentType )
        throws TransferException
    {
        return transferManager.publish( new Resource( location, toPath( ref, filename ) ), stream, length, contentType );
    }

    private String toPath( final ProjectRef ref, final String filename )
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( ref.getGroupId()
                      .replace( '.', '/' ) )
          .append( '/' )
          .append( ref.getArtifactId() );

        if ( ref instanceof ProjectVersionRef )
        {
            // FIXME: Local snapshot handling...which may also need to be managed in the cache provider...
            sb.append( '/' )
              .append( ( (ProjectVersionRef) ref ).getVersionString() );
        }

        sb.append( '/' )
          .append( filename == null ? DEFAULT_FILENAME : filename );

        return sb.toString();
    }

    private String toPath( final String groupId, final String filename )
    {
        return String.format( "%s/%s", groupId.replace( '.', '/' ), filename == null ? DEFAULT_FILENAME : filename );
    }

}
