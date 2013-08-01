package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

public class ArtifactMetadataManager
{

    public static final String DEFAULT_FILENAME = "maven-metadata.xml";

    private final TransferManager transferManager;

    public ArtifactMetadataManager( final TransferManager transferManager )
    {
        this.transferManager = transferManager;
    }

    public boolean delete( final Location location, final ProjectRef ref )
        throws TransferException
    {
        return delete( location, ref, null );
    }

    public boolean delete( final Location location, final ProjectRef ref, final String filename )
        throws TransferException
    {
        final String path = toPath( ref, filename );
        return transferManager.delete( location, path );
    }

    public boolean delete( final Location location, final String groupId )
        throws TransferException
    {
        return delete( location, groupId, null );
    }

    public boolean delete( final Location location, final String groupId, final String filename )
        throws TransferException
    {
        final String path = toPath( groupId, filename );
        return transferManager.delete( location, path );
    }

    public boolean deleteAll( final List<? extends Location> locations, final String groupId )
        throws TransferException
    {
        return deleteAll( locations, groupId, null );
    }

    public boolean deleteAll( final List<? extends Location> locations, final String groupId, final String filename )
        throws TransferException
    {
        return transferManager.deleteAll( locations, toPath( groupId, filename ) );
    }

    public boolean deleteAll( final List<? extends Location> locations, final ProjectRef ref )
        throws TransferException
    {
        return deleteAll( locations, ref, null );
    }

    public boolean deleteAll( final List<? extends Location> locations, final ProjectRef ref, final String filename )
        throws TransferException
    {
        return transferManager.deleteAll( locations, toPath( ref, filename ) );
    }

    public Transfer retrieve( final Location location, final String groupId )
        throws TransferException
    {
        return retrieve( location, groupId, null );
    }

    public Transfer retrieve( final Location location, final String groupId, final String filename )
        throws TransferException
    {
        return transferManager.retrieve( location, toPath( groupId, filename ) );
    }

    public Transfer retrieve( final Location location, final ProjectRef ref )
        throws TransferException
    {
        return retrieve( location, ref, null );
    }

    public Transfer retrieve( final Location location, final ProjectRef ref, final String filename )
        throws TransferException
    {
        return transferManager.retrieve( location, toPath( ref, filename ) );
    }

    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final String groupId )
        throws TransferException
    {
        return retrieveAll( locations, groupId, null );
    }

    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final String groupId,
                                      final String filename )
        throws TransferException
    {
        return transferManager.retrieveAll( locations, toPath( groupId, filename ) );
    }

    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final ProjectRef ref )
        throws TransferException
    {
        return retrieveAll( locations, ref, null );
    }

    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final ProjectRef ref,
                                      final String filename )
        throws TransferException
    {
        return transferManager.retrieveAll( locations, toPath( ref, filename ) );
    }

    public Transfer retrieveFirst( final List<? extends Location> locations, final String groupId )
        throws TransferException
    {
        return retrieveFirst( locations, groupId, null );
    }

    public Transfer retrieveFirst( final List<? extends Location> locations, final String groupId, final String filename )
        throws TransferException
    {
        return transferManager.retrieveFirst( locations, toPath( groupId, filename ) );
    }

    public Transfer retrieveFirst( final List<? extends Location> locations, final ProjectRef ref )
        throws TransferException
    {
        return retrieveFirst( locations, ref, null );
    }

    public Transfer retrieveFirst( final List<? extends Location> locations, final ProjectRef ref, final String filename )
        throws TransferException
    {
        return transferManager.retrieveFirst( locations, toPath( ref, filename ) );
    }

    public Transfer store( final Location location, final String groupId, final InputStream stream )
        throws TransferException
    {
        return store( location, groupId, null, stream );
    }

    public Transfer store( final Location location, final String groupId, final String filename,
                           final InputStream stream )
        throws TransferException
    {
        return transferManager.store( location, toPath( groupId, filename ), stream );
    }

    public Transfer store( final Location location, final ProjectRef ref, final InputStream stream )
        throws TransferException
    {
        return store( location, ref, null, stream );
    }

    public Transfer store( final Location location, final ProjectRef ref, final String filename,
                           final InputStream stream )
        throws TransferException
    {
        return transferManager.store( location, toPath( ref, filename ), stream );
    }

    public boolean publish( final Location location, final String groupId, final InputStream stream, final long length )
        throws TransferException
    {
        return publish( location, groupId, null, stream, length, null );
    }

    public boolean publish( final Location location, final String groupId, final String filename,
                            final InputStream stream, final long length, final String contentType )
        throws TransferException
    {
        return transferManager.publish( location, toPath( groupId, filename ), stream, length, contentType );
    }

    public boolean publish( final Location location, final ProjectRef ref, final InputStream stream, final long length )
        throws TransferException
    {
        return publish( location, ref, null, stream, length, null );
    }

    public boolean publish( final Location location, final ProjectRef ref, final String filename,
                            final InputStream stream, final long length, final String contentType )
        throws TransferException
    {
        return transferManager.publish( location, toPath( ref, filename ), stream, length, contentType );
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
