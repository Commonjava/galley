package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.util.ArtifactPathInfo;

public interface TransferManager
{

    Transfer retrieveFirst( List<? extends Location> stores, String path )
        throws TransferException;

    Set<Transfer> retrieveAll( List<? extends Location> stores, String path )
        throws TransferException;

    Transfer retrieve( Resource resource )
        throws TransferException;

    Transfer store( Resource resource, InputStream stream )
        throws TransferException;

    Transfer store( List<? extends Location> stores, String path, InputStream stream )
        throws TransferException;

    ArtifactPathInfo parsePathInfo( String path );

    Transfer getStoreRootDirectory( Location key );

    Transfer getCacheReference( Resource resource );

    boolean deleteAll( List<? extends Location> stores, String path )
        throws TransferException;

    boolean delete( Resource resource )
        throws TransferException;

    boolean publish( Resource resource, InputStream stream, long length )
        throws TransferException;

    boolean publish( Resource resource, InputStream stream, long length, String contentType )
        throws TransferException;

    ListingResult list( Resource resource )
        throws TransferException;

}