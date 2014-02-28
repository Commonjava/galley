/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.maven.atlas.ident.util.StringFormat;
import org.commonjava.maven.galley.event.FileErrorEvent;
import org.commonjava.maven.galley.event.FileNotFoundEvent;
import org.commonjava.maven.galley.internal.xfer.BatchRetriever;
import org.commonjava.maven.galley.internal.xfer.DownloadHandler;
import org.commonjava.maven.galley.internal.xfer.ExistenceHandler;
import org.commonjava.maven.galley.internal.xfer.ListingHandler;
import org.commonjava.maven.galley.internal.xfer.UploadHandler;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferBatch;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.model.VirtualResource;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferManagerImpl
    implements TransferManager
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private CacheProvider cacheProvider;

    @Inject
    private NotFoundCache nfc;

    @Inject
    private TransportManager transportManager;

    @Inject
    private FileEventManager fileEventManager;

    @Inject
    private DownloadHandler downloader;

    @Inject
    private UploadHandler uploader;

    @Inject
    private ListingHandler lister;

    @Inject
    private ExistenceHandler exister;

    @Inject
    @ExecutorConfig( threads = 12, daemon = true, named = "galley-batching", priority = 8 )
    private ExecutorService executor;

    protected TransferManagerImpl()
    {
    }

    public TransferManagerImpl( final TransportManager transportManager, final CacheProvider cacheProvider, final NotFoundCache nfc,
                                final FileEventManager fileEventManager, final DownloadHandler downloader, final UploadHandler uploader,
                                final ListingHandler lister, final ExistenceHandler exister, final ExecutorService executor )
    {
        this.transportManager = transportManager;
        this.cacheProvider = cacheProvider;
        this.nfc = nfc;
        this.fileEventManager = fileEventManager;
        this.downloader = downloader;
        this.uploader = uploader;
        this.lister = lister;
        this.exister = exister;
        this.executor = executor;
    }

    @Override
    public boolean exists( final ConcreteResource resource )
        throws TransferException
    {
        return exists( resource, false );
    }

    @Override
    public ConcreteResource findFirstExisting( final VirtualResource virt )
        throws TransferException
    {
        for ( final ConcreteResource res : virt )
        {
            if ( exists( res, true ) )
            {
                return res;
            }
        }

        return null;
    }

    @Override
    public List<ConcreteResource> findAllExisting( final VirtualResource virt )
        throws TransferException
    {
        final List<ConcreteResource> results = new ArrayList<ConcreteResource>();
        for ( final ConcreteResource res : virt )
        {
            if ( exists( res, true ) )
            {
                results.add( res );
            }
        }

        return results;
    }

    private boolean exists( final ConcreteResource resource, final boolean suppressFailures )
        throws TransferException
    {
        final Transfer cached = getCacheReference( resource );
        if ( cached.exists() )
        {
            return true;
        }

        return exister.exists( resource, getTimeoutSeconds( resource ), getTransport( resource ), suppressFailures );
    }

    @Override
    public List<ListingResult> listAll( final VirtualResource virt )
        throws TransferException
    {
        final List<ListingResult> results = new ArrayList<ListingResult>();
        for ( final ConcreteResource res : virt )
        {
            final ListingResult result = doList( res, true );
            if ( result != null )
            {
                results.add( result );
            }
        }

        return results;
    }

    @Override
    public ListingResult list( final ConcreteResource resource )
        throws TransferException
    {
        return doList( resource, false );
    }

    private ListingResult doList( final ConcreteResource resource, final boolean suppressFailures )
        throws TransferException
    {
        final Transfer cached = getCacheReference( resource );
        ListingResult cacheResult = null;
        if ( cached.exists() )
        {
            if ( !cached.isDirectory() )
            {
                throw new TransferException( "Cannot list: {}. It does not appear to be a directory.", resource );
            }
            else
            {
                cacheResult = new ListingResult( resource, cached.list() );
            }
        }

        final int timeoutSeconds = getTimeoutSeconds( resource );
        final ListingResult remoteResult = lister.list( resource, timeoutSeconds, getTransport( resource ), suppressFailures );

        ListingResult result;
        if ( cacheResult != null && remoteResult != null )
        {
            result = cacheResult.mergeWith( remoteResult );
        }
        else if ( cacheResult != null )
        {
            result = cacheResult;
        }
        else
        {
            result = remoteResult;
        }

        return result;
    }

    private Transport getTransport( final ConcreteResource resource )
        throws TransferException
    {
        final Transport transport = transportManager.getTransport( resource );
        if ( transport == null )
        {
            if ( resource.getLocationUri() == null )
            {
                logger.debug( "NFC: No remote URI. Marking as missing: {}", resource );
                nfc.addMissing( resource );
                return null;
            }

            throw new TransferException( "No transports available to handle: {} with location type: {}", resource, resource.getLocation()
                                                                                                                           .getClass()
                                                                                                                           .getSimpleName() );
        }

        return transport;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieveFirst(java.util.List, java.lang.String)
     */
    @Override
    public Transfer retrieveFirst( final VirtualResource virt )
        throws TransferException
    {
        Transfer target = null;

        TransferException lastError = null;
        for ( final ConcreteResource res : virt )
        {
            if ( res == null )
            {
                continue;
            }

            try
            {
                target = retrieve( res, true );
                lastError = null;
                if ( target != null && target.exists() )
                {
                    return target;
                }
            }
            catch ( final TransferException e )
            {
                lastError = e;
            }
        }

        if ( lastError != null )
        {
            throw lastError;
        }

        fileEventManager.fire( new FileNotFoundEvent( virt ) );
        return null;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieveAll(java.util.List, java.lang.String)
     */
    @Override
    public List<Transfer> retrieveAll( final VirtualResource virt )
        throws TransferException
    {
        TransferBatch batch = new TransferBatch( Collections.singleton( virt ) );
        batch = batchRetrieveAll( batch );

        return new ArrayList<Transfer>( batch.getTransfers()
                                             .values() );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieve(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public Transfer retrieve( final ConcreteResource resource )
        throws TransferException
    {
        return retrieve( resource, false );
    }

    public Transfer retrieve( final ConcreteResource resource, final boolean suppressFailures )
        throws TransferException
    {
        // TODO: Handle the case where storage isn't allowed? 
        // NOTE: This would expand the notion out from simply: 
        //    "don't allow storing new stuff"
        // to:
        //    "don't ever cache this stuff"
        Transfer target = null;
        try
        {
            // TODO: (see above re:storing) Handle things like local archives that really don't need to be cached...
            target = getCacheReference( resource );

            if ( target.exists() )
            {
                return target;
            }

            if ( !resource.allowsDownloading() )
            {
                return null;
            }

            final Transfer retrieved =
                downloader.download( resource, target, getTimeoutSeconds( resource ), getTransport( resource ), suppressFailures );

            if ( retrieved != null && retrieved.exists() && !target.equals( retrieved ) )
            {
                cacheProvider.createAlias( retrieved.getResource(), target.getResource() );
            }

            if ( target.exists() )
            {
                logger.debug( "DOWNLOADED: {}", resource );
                return target;
            }
            else
            {
                logger.debug( "NOT DOWNLOADED: {}", resource );
                return null;
            }
        }
        catch ( final TransferException e )
        {
            fileEventManager.fire( new FileErrorEvent( target, e ) );
            throw e;
        }
        catch ( final IOException e )
        {
            final TransferException error = new TransferException( "Failed to download: {}. Reason: {}", e, resource, e.getMessage() );

            fileEventManager.fire( new FileErrorEvent( target, error ) );
            throw error;
        }
    }

    @Override
    public Transfer store( final ConcreteResource resource, final InputStream stream )
        throws TransferException
    {
        if ( !resource.allowsStoring() )
        {
            throw new TransferException( "Storing not allowed in: {}", resource );
        }

        final Transfer target = getCacheReference( resource );

        logger.info( "STORE {}", target.getResource() );

        OutputStream out = null;
        try
        {
            out = target.openOutputStream( TransferOperation.UPLOAD );
            copy( stream, out );
        }
        catch ( final IOException e )
        {
            throw new TransferException( "Failed to store: {}. Reason: {}", e, resource, e.getMessage() );
        }
        finally
        {
            closeQuietly( out );
        }

        return target;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#getStoreRootDirectory(org.commonjava.maven.galley.model.Location)
     */
    @Override
    public Transfer getStoreRootDirectory( final Location key )
    {
        return cacheProvider.getTransfer( new ConcreteResource( key ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#getCacheReference(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public Transfer getCacheReference( final ConcreteResource resource )
    {
        return cacheProvider.getTransfer( resource );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#deleteAll(java.util.List, java.lang.String)
     */
    @Override
    public boolean deleteAll( final VirtualResource virt )
        throws TransferException
    {
        boolean result = false;
        for ( final ConcreteResource res : virt )
        {
            result = delete( res ) || result;
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#delete(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public boolean delete( final ConcreteResource resource )
        throws TransferException
    {
        final Transfer item = getCacheReference( resource );
        return doDelete( item );
    }

    private Boolean doDelete( final Transfer item )
        throws TransferException
    {
        if ( !item.exists() )
        {
            return false;
        }

        logger.info( "DELETE {}", item.getResource() );

        if ( item.isDirectory() )
        {
            final String[] listing = item.list();
            for ( final String sub : listing )
            {
                if ( !doDelete( item.getChild( sub ) ) )
                {
                    return false;
                }
            }
        }
        else
        {
            try
            {
                if ( !item.delete() )
                {
                    throw new TransferException( "Failed to delete: {}.", item );
                }
            }
            catch ( final IOException e )
            {
                throw new TransferException( "Failed to delete stored location: {}. Reason: {}", e, item, e.getMessage() );
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final ConcreteResource resource, final InputStream stream, final long length )
        throws TransferException
    {
        return publish( resource, stream, length, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long, java.lang.String)
     */
    @Override
    public boolean publish( final ConcreteResource resource, final InputStream stream, final long length, final String contentType )
        throws TransferException
    {
        return uploader.upload( resource, stream, length, contentType, getTimeoutSeconds( resource ), getTransport( resource ) );
    }

    private int getTimeoutSeconds( final ConcreteResource resource )
    {
        Integer timeoutSeconds = resource.getAttribute( Location.CONNECTION_TIMEOUT_SECONDS, Integer.class );
        if ( timeoutSeconds == null )
        {
            timeoutSeconds = Location.DEFAULT_CONNECTION_TIMEOUT_SECONDS;
        }

        return timeoutSeconds;
    }

    @Override
    public <T extends TransferBatch> T batchRetrieve( final T batch )
        throws TransferException
    {
        return doBatch( batch.getResources(), batch, true );
    }

    @Override
    public <T extends TransferBatch> T batchRetrieveAll( final T batch )
        throws TransferException
    {
        final Set<Resource> resources = batch.getResources();
        for ( final Resource resource : new HashSet<Resource>( resources ) )
        {
            if ( resource instanceof VirtualResource )
            {
                resources.remove( resource );
                for ( final Resource r : (VirtualResource) resource )
                {
                    resources.add( r );
                }
            }
        }

        return doBatch( resources, batch, false );
    }

    private <T extends TransferBatch> T doBatch( final Set<Resource> resources, final T batch, final boolean suppressFailures )
        throws TransferException
    {
        logger.info( "Attempting to batch-retrieve {} resources", resources.size() );

        final Set<BatchRetriever> retrievers = new HashSet<BatchRetriever>( resources.size() );
        for ( final Resource resource : resources )
        {
            retrievers.add( new BatchRetriever( this, resource, suppressFailures ) );
        }

        final Map<ConcreteResource, TransferException> errors = new HashMap<ConcreteResource, TransferException>();
        final Map<ConcreteResource, Transfer> transfers = new HashMap<ConcreteResource, Transfer>();

        int tries = 1;
        while ( !retrievers.isEmpty() )
        {
            logger.debug( "Starting attempt #{} to retrieve batch (batch size is currently: {})", tries, retrievers.size() );
            final CountDownLatch latch = new CountDownLatch( resources.size() );
            for ( final BatchRetriever retriever : retrievers )
            {
                retriever.setLatch( latch );
                executor.execute( retriever );
            }

            try
            {
                latch.await();
            }
            catch ( final InterruptedException e )
            {
                logger.error( "{}", e, new StringFormat( "Failed to wait for batch retrieval attempts to complete: {}", e.getMessage() ) );
            }

            for ( final BatchRetriever retriever : new HashSet<BatchRetriever>( retrievers ) )
            {
                final ConcreteResource resource = retriever.getLastTry();
                final TransferException error = retriever.getError();
                if ( error != null )
                {
                    errors.put( resource, error );
                    retrievers.remove( retriever );
                    logger.warn( "ERROR: {}...{}", error, resource, error.getMessage() );
                    continue;
                }

                final Transfer transfer = retriever.getTransfer();
                if ( transfer != null && transfer.exists() )
                {
                    transfers.put( resource, transfer );
                    retrievers.remove( retriever );
                    logger.debug( "Completed: {}", resource );
                    continue;
                }

                if ( !retriever.hasMoreTries() )
                {
                    logger.debug( "Not completed, but out of tries: {}", resource );
                    retrievers.remove( retriever );
                }
            }

            tries++;
        }

        batch.setErrors( errors );
        batch.setTransfers( transfers );

        return batch;
    }

}
