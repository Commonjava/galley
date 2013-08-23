package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.util.ArtifactPathInfo;

public interface TransferManager
{

    Transfer retrieveFirst( List<? extends Location> stores, String path )
        throws TransferException;

    Set<Transfer> retrieveAll( List<? extends Location> stores, String path )
        throws TransferException;

    Transfer retrieve( Location store, String path )
        throws TransferException;

    Transfer store( Location deploy, String path, InputStream stream )
        throws TransferException;

    Transfer store( List<? extends Location> stores, String path, InputStream stream )
        throws TransferException;

    ArtifactPathInfo parsePathInfo( String path );

    Transfer getStoreRootDirectory( Location key );

    Transfer getCacheReference( Location store, String... path );

    boolean deleteAll( List<? extends Location> stores, String path )
        throws TransferException;

    boolean delete( Location store, String path )
        throws TransferException;

    boolean publish( Location location, String path, InputStream stream, long length )
        throws TransferException;

    boolean publish( Location location, String path, InputStream stream, long length, String contentType )
        throws TransferException;

    ListingResult list( Location location, String path )
        throws TransferException;

}