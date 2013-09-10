package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferBatch;
import org.commonjava.maven.galley.model.VirtualResource;

public interface TransferManager
{

    <T extends TransferBatch> T batchRetrieve( T batch )
        throws TransferException;

    <T extends TransferBatch> T batchRetrieveAll( T batch )
        throws TransferException;

    Transfer retrieveFirst( VirtualResource resource )
        throws TransferException;

    List<Transfer> retrieveAll( VirtualResource resource )
        throws TransferException;

    Transfer retrieve( ConcreteResource resource )
        throws TransferException;

    Transfer store( ConcreteResource resource, InputStream stream )
        throws TransferException;

    Transfer store( VirtualResource resource, InputStream stream )
        throws TransferException;

    Transfer getStoreRootDirectory( Location key );

    Transfer getCacheReference( ConcreteResource resource );

    boolean deleteAll( VirtualResource resource )
        throws TransferException;

    boolean delete( ConcreteResource resource )
        throws TransferException;

    boolean publish( ConcreteResource resource, InputStream stream, long length )
        throws TransferException;

    boolean publish( ConcreteResource resource, InputStream stream, long length, String contentType )
        throws TransferException;

    ListingResult list( ConcreteResource resource )
        throws TransferException;

    List<ListingResult> listAll( VirtualResource resource )
        throws TransferException;

    boolean exists( ConcreteResource resource )
        throws TransferException;

    ConcreteResource findFirstExisting( VirtualResource resource )
        throws TransferException;

    List<ConcreteResource> findAllExisting( VirtualResource resource )
        throws TransferException;

}