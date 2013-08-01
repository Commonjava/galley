package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

public class ArtifactManager
{

    private final TransferManager transferManager;

    public ArtifactManager( final TransferManager transferManager )
    {
        this.transferManager = transferManager;
    }

    public boolean delete( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        final String path = toPath( ref );
        return transferManager.delete( location, path );
    }

    public boolean deleteAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.deleteAll( locations, toPath( ref ) );
    }

    public Transfer retrieve( final Location location, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieve( location, toPath( ref ) );
    }

    public Set<Transfer> retrieveAll( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveAll( locations, toPath( ref ) );
    }

    public Transfer retrieveFirst( final List<? extends Location> locations, final ArtifactRef ref )
        throws TransferException
    {
        return transferManager.retrieveFirst( locations, toPath( ref ) );
    }

    public Transfer store( final Location location, final ArtifactRef ref, final InputStream stream )
        throws TransferException
    {
        return transferManager.store( location, toPath( ref ), stream );
    }

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
