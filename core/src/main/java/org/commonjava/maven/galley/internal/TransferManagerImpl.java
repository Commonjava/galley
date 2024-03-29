/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.internal;

import org.apache.commons.io.IOUtils;
import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.cdi.util.weft.WeftManaged;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferLocationException;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.event.FileErrorEvent;
import org.commonjava.maven.galley.event.FileNotFoundEvent;
import org.commonjava.maven.galley.internal.xfer.BatchRetriever;
import org.commonjava.maven.galley.internal.xfer.DownloadHandler;
import org.commonjava.maven.galley.internal.xfer.ExistenceHandler;
import org.commonjava.maven.galley.internal.xfer.ListingHandler;
import org.commonjava.maven.galley.internal.xfer.UploadHandler;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferBatch;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.model.VirtualResource;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.lang.StringUtils.join;
import static org.commonjava.maven.galley.model.Transfer.DELETE_CONTENT_LOG;
import static org.commonjava.maven.galley.util.LocationUtils.getTimeoutSeconds;

@ApplicationScoped
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
    private SpecialPathManager specialPathManager;

    @Inject
    @WeftManaged
    @ExecutorConfig( threads = 12, named = "galley-batching", priority = 8 )
    private ExecutorService executorService;

    private ExecutorCompletionService<BatchRetriever> batchExecutor;

    @SuppressWarnings( "unused" )
    protected TransferManagerImpl()
    {
    }

    public TransferManagerImpl( final TransportManager transportManager, final CacheProvider cacheProvider,
                                final NotFoundCache nfc, final FileEventManager fileEventManager,
                                final DownloadHandler downloader, final UploadHandler uploader,
                                final ListingHandler lister, final ExistenceHandler exister,
                                final SpecialPathManager specialPathManager, final ExecutorService executorService )
    {
        this.transportManager = transportManager;
        this.cacheProvider = cacheProvider;
        this.nfc = nfc;
        this.fileEventManager = fileEventManager;
        this.downloader = downloader;
        this.uploader = uploader;
        this.lister = lister;
        this.exister = exister;
        this.specialPathManager = specialPathManager;
        this.executorService = executorService;
        init();
    }

    @PostConstruct
    public void init()
    {
        batchExecutor = new ExecutorCompletionService<>( executorService );
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
        final List<ConcreteResource> results = new ArrayList<>();
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
        return cached.exists() || exister.exists( resource, cached, getTimeoutSeconds( resource ),
                                                  getTransport( resource ), suppressFailures );

    }

    @Override
    public List<ListingResult> listAll( final VirtualResource virt )
            throws TransferException
    {
        return listAll( virt, new EventMetadata(  ) );
    }

    @Override
    public List<ListingResult> listAll( final VirtualResource virt, final EventMetadata metadata )
        throws TransferException
    {
        final Map<ConcreteResource, Future<ListingResult>> futureList = new HashMap<>();
        for ( final ConcreteResource res : virt )
        {
            final Future<ListingResult> listingFuture = executorService.submit( () -> doList( res, true, metadata ) );
            futureList.put( res, listingFuture );
        }

        final List<ListingResult> results = new ArrayList<>();
        for ( Map.Entry<ConcreteResource, Future<ListingResult>> entry : futureList.entrySet() )
        {
            Future<ListingResult> listingFuture = entry.getValue();
            ConcreteResource res = entry.getKey();
            ListingResult listing;
            try
            {
                listing = listingFuture.get();
            }
            catch ( InterruptedException ex )
            {
                throw new TransferException( "Listing of %s was interrupted", ex, res );
            }
            catch ( ExecutionException ex )
            {
                throw new TransferException( "Listing of %s threw an error: %s", ex, res, ex );
            }

            if ( listing != null )
            {
                results.add( listing );
            }
        }

        return results;

    }

    @Override
    public ListingResult list( final ConcreteResource resource )
            throws TransferException
    {
        return list( resource, new EventMetadata(  ) );
    }

    @Override
    public ListingResult list( final ConcreteResource resource, final EventMetadata metadata )
        throws TransferException
    {
        return doList( resource, false, metadata );
    }

    private ListingResult doList( final ConcreteResource resource, final boolean suppressFailures, EventMetadata metadata )
        throws TransferException
    {
        logger.debug( "doList, resource: {}, metadata: {}", resource, metadata );

        final Transfer cachedListing = getCacheReference( resource.getChild( ".listing.txt" ) );
        Set<String> filenames = new HashSet<>();
        if ( cachedListing.exists() )
        {
            logger.debug( "Listing file exists, {}", cachedListing );

            InputStream stream = null;
            try
            {
                stream = cachedListing.openInputStream();
                filenames.addAll( IOUtils.readLines( stream, "UTF-8" ) );

                logger.debug( "Got cached listing:\n\n{}\n\n", filenames );
            }
            catch ( final IOException e )
            {
                throw new TransferException( "Failed to read listing from cached file: %s. Reason: %s", e,
                                             cachedListing, e.getMessage() );
            }
            finally
            {
                closeQuietly( stream );
            }
        }
        else
        {
            final Transfer cached = getCacheReference( resource );
            if ( cached.exists() )
            {
                logger.debug( "Try cached transfer {}", cached );
                if ( cached.isFile() )
                {
                    throw new TransferException( "Cannot list: {}. It does not appear to be a directory.", resource );
                }
                else
                {
                    try
                    {
                        // This is fairly stupid, but we need to append '/' to the end of directories in the listing so content processors can figure
                        // out what to do with them.
                        String[] fnames = cached.list();
                        if ( fnames != null && fnames.length > 0 )
                        {
                            for ( String fname : fnames )
                            {
                                final ConcreteResource child = resource.getChild( fname );
                                final Transfer childRef = getCacheReference( child );
                                if ( childRef.isFile() )
                                {
                                    filenames.add( fname );
                                }
                                else
                                {
                                    /*
                                    As a way of clean-up, this try to list all sub-folders to see if they are empty.
                                    e.g., if a dir has 1000 sub folders, it actually will do 1000 more listing operations.
                                    On some storage system, this can cause serious performance problem.
                                    The clean-up is useful to some extent. e.g, if there is a maven version folder
                                    and for some reason it is empty, the metadata generation will skip it.
                                    To fix the performance and still keep the ability of empty check, I use a flag in event metadata
                                    to indicate whether the listing need to filter out empty folders.
                                    */
                                    if ( TRUE.equals( metadata.get( ALLOW_REMOVE_EMPTY_DIRECTORY ) ) && isEmptyFolder( childRef ) )
                                    {
                                        // if the directory is there but it's empty, we should delete it
                                        logger.info( "Delete empty folder, {}", childRef.getFullPath() );
                                        deleteQuietly( childRef );
                                    }
                                    else
                                    {
                                        if ( fname.endsWith( "/" ) )
                                        {
                                            filenames.add( fname );
                                        }
                                        else
                                        {
                                            filenames.add( fname + "/" );
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch ( final IOException e )
                    {
                        throw new TransferException( "Listing failed: {}. Reason: {}", e, resource, e.getMessage() );
                    }
                }
            }


            final boolean allowRemoteListDownload =
                    metadata.get( ALLOW_REMOTE_LISTING_DOWNLOAD ) == null || (Boolean) metadata.get(
                            ALLOW_REMOTE_LISTING_DOWNLOAD );

            logger.debug( "Try remote listing, allowRemoteListDownload: {}", allowRemoteListDownload );

            if ( resource.getLocation().allowsDownloading() && allowRemoteListDownload )
            {

                final int timeoutSeconds = getTimeoutSeconds( resource );
                Transport transport = getTransport( resource );

                final ListingResult remoteResult =
                        lister.list( resource, cachedListing, timeoutSeconds, transport, suppressFailures );
                logger.debug( "Remote listing done, remoteResult: {}", remoteResult );

                if ( remoteResult != null )
                {
                    String[] remoteListing = remoteResult.getListing();
                    if ( remoteListing != null && remoteListing.length > 0 )
                    {
                        final TransferDecoratorManager decorator = cachedListing.getDecorator();
                        if ( decorator != null )
                        {
                            try
                            {
                                logger.debug( "Un-decorated listing:\n\n{}\n\n", Arrays.asList( remoteListing ) );
                                remoteListing = decorator.decorateListing( cachedListing.getParent(), remoteListing, metadata );
                            }
                            catch ( final IOException e )
                            {
                                logger.error( "Failed to decorate directory listing for: " + resource, e );
                                remoteListing = null;
                            }
                        }
                    }

                    if ( remoteListing != null && remoteListing.length > 0  )
                    {
                        if ( transport != null && transport.allowsCaching() )
                        {
                            writeListingTxt( cachedListing, remoteListing, resource );
                        }

                        filenames.addAll( Arrays.asList( remoteListing ) );
                    }
                }
            }
        }

        logger.debug( "Listing before non-listable file removal:\n\n{}\n\n", filenames );

        List<String> resultingNames = new ArrayList<>( filenames.size() );
        for( String fname : filenames )
        {
            ConcreteResource child = resource.getChild( fname );

            SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( child, metadata.getPackageType() );
            if ( specialPathInfo != null && !specialPathInfo.isListable() )
            {
                continue;
            }

            resultingNames.add( fname );
        }

        logger.debug( "Final listing result:\n\n{}\n\n", resultingNames );

        return new ListingResult( resource, resultingNames.toArray( new String[0] ) );
    }

    private void deleteQuietly( Transfer childRef )
    {
        try
        {
            forceDelete(childRef);
        }
        catch ( IOException e )
        {
            logger.warn( "Delete failed, childRef: " + childRef, e );
        }
    }

    private void forceDelete( Transfer childRef ) throws IOException
    {
        final ConcreteResource resource = new ConcreteResource( childRef.getLocation(), childRef.getPath() )
        {
            @Override
            public boolean allowsDeletion()
            {
                return true;
            }
        };
        final Transfer forced = getCacheReference( resource );
        forced.delete();
    }

    private void writeListingTxt( Transfer cachedListing, String[] remoteListing, ConcreteResource resource )
    {
        logger.debug( "Writing listing:\n\n{}\n\nto: {}", remoteListing, cachedListing );

        try (OutputStream stream = cachedListing.openOutputStream( TransferOperation.DOWNLOAD ))
        {
            stream.write( join( remoteListing, "\n" ).getBytes( StandardCharsets.UTF_8 ) );
        }
        catch ( final IOException e )
        {
            logger.debug( "Failed to store directory listing for: {}. Reason: {}", resource, e.getMessage(), e );
        }
    }

    private boolean isEmptyFolder( Transfer childRef ) throws IOException
    {
        if ( childRef.isDirectory() )
        {
            String[] list = childRef.list();
            return list == null || list.length == 0;
        }
        return false;
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
        return retrieveFirst( virt, new EventMetadata() );
    }

    @Override
    public Transfer retrieveFirst( final VirtualResource virt, final EventMetadata eventMetadata )
        throws TransferException
    {
        Transfer target;

        TransferException lastError = null;
        int tries = 0;
        for ( final ConcreteResource res : virt )
        {
            tries++;

            if ( res == null )
            {
                continue;
            }

            try
            {
                target = retrieve( res, true, eventMetadata );
                lastError = null;
                if ( target != null && target.exists() )
                {
                    return target;
                }
            }
            catch ( final TransferException e )
            {
                logger.warn( "Failed to retrieve: {}. {} more tries. (Reason: {})", res, ( virt.toConcreteResources()
                                                                                               .size() - tries ),
                             e.getMessage() );
                lastError = e;
            }
        }

        if ( lastError != null )
        {
            throw lastError;
        }

        fileEventManager.fire( new FileNotFoundEvent( virt, eventMetadata ) );
        return null;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieveAll(java.util.List, java.lang.String)
     */
    @Override
    public List<Transfer> retrieveAll( final VirtualResource virt )
        throws TransferException
    {
        return retrieveAll( virt, new EventMetadata() );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieveAll(java.util.List, java.lang.String)
     */
    @Override
    public List<Transfer> retrieveAll( final VirtualResource virt , final EventMetadata eventMetadata  )
        throws TransferException
    {
        TransferBatch batch = new TransferBatch( Collections.singleton( virt ) );
        batch = batchRetrieveAll( batch, eventMetadata );

        return new ArrayList<>( batch.getTransfers()
                                             .values() );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieve(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public Transfer retrieve( final ConcreteResource resource )
        throws TransferException
    {
        return retrieve( resource, false, new EventMetadata() );
    }

    @Override
    public Transfer retrieve( final ConcreteResource resource, final boolean suppressFailures )
        throws TransferException
    {
        return retrieve( resource, suppressFailures, new EventMetadata() );
    }

    @Override
    public Transfer retrieve( final ConcreteResource resource, final boolean suppressFailures,
                              final EventMetadata eventMetadata )
        throws TransferException
    {
        //        logger.info( "Attempting to resolve: {}", resource );

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
                logger.debug( "Using cached copy of: {}", target );
                return target;
            }

            SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( resource, eventMetadata.getPackageType() );
            if ( !resource.allowsDownloading() || ( specialPathInfo != null && !specialPathInfo.isRetrievable() ) )
            {
                logger.debug( "Download not allowed for: {}. Returning null transfer.", resource );
                return null;
            }

            final Transfer retrieved =
                downloader.download( resource, target, getTimeoutSeconds( resource ), getTransport( resource ),
                                     suppressFailures, eventMetadata );

            if ( retrieved != null && retrieved.exists() && !target.equals( retrieved ) )
            {
                if ( specialPathInfo == null || specialPathInfo.isCachable() )
                {
                    cacheProvider.createAlias( retrieved.getResource(), target.getResource() );
                }
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
            fileEventManager.fire( new FileErrorEvent( target, e, eventMetadata ) );
            throw e;
        }
        catch ( final IOException e )
        {
            final TransferException error =
                new TransferException( "Failed to download: {}. Reason: {}", e, resource, e.getMessage() );

            fileEventManager.fire( new FileErrorEvent( target, error, eventMetadata ) );
            throw error;
        }
    }

    @Override
    public Transfer store( final ConcreteResource resource, final InputStream stream )
        throws TransferException
    {
        return store( resource, stream, new EventMetadata() );
    }

    @Override
    public Transfer store( ConcreteResource resource , final InputStream stream , final EventMetadata eventMetadata  )
        throws TransferException
    {
        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( resource, eventMetadata.getPackageType() );
        if ( !resource.allowsStoring() || ( specialPathInfo != null && !specialPathInfo.isStorable() ) )
        {
            throw new TransferException( "Storing not allowed for: {}", resource );
        }

        final Transfer target = getCacheReference( resource );

        logger.info( "STORE {}", target.getResource() );

        OutputStream out = null;
        try
        {
            out = target.openOutputStream( TransferOperation.UPLOAD, true, eventMetadata );
            copy( stream, out );

            nfc.clearMissing( resource );
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
        return deleteAll( virt, new EventMetadata() );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#deleteAll(java.util.List, java.lang.String)
     */
    @Override
    public boolean deleteAll( final VirtualResource virt , final EventMetadata eventMetadata  )
        throws TransferException
    {
        boolean result = false;
        for ( final ConcreteResource res : virt )
        {
            result = delete( res, new EventMetadata() ) || result;
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
        return delete( resource, new EventMetadata() );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#delete(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public boolean delete( final ConcreteResource resource, final EventMetadata eventMetadata )
        throws TransferException
    {
        if ( !resource.allowsDeletion() )
        {
            throw new TransferException( "Deletion not allowed for: {}", resource );
        }

        final Transfer item = getCacheReference( resource );
        return doDelete( item, eventMetadata );
    }

    private Boolean doDelete( final Transfer item, final EventMetadata eventMetadata )
        throws TransferException
    {
        if ( !item.exists() )
        {
            return false;
        }

        Logger contentLogger = LoggerFactory.getLogger( DELETE_CONTENT_LOG );
        contentLogger.info( "BEGIN: Delete {} ({})", item.getResource(), eventMetadata );

        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( item, eventMetadata.getPackageType() );
        if ( specialPathInfo != null && !specialPathInfo.isDeletable() )
        {
            throw new TransferException( "Deleting not allowed for: %s", item );
        }

        if ( item.isDirectory() )
        {
            String[] listing;
            try
            {
                listing = item.list();
            }
            catch ( final IOException e )
            {
                throw new TransferException( "Delete failed: {}. Reason: cannot list directory due to: {}", e, item,
                                             e.getMessage() );
            }

            for ( final String sub : listing )
            {
                if ( !doDelete( item.getChild( sub ), eventMetadata ) )
                {
                    contentLogger.info( "FAIL: Delete: {}", item.getResource() );
                    return false;
                }
            }
        }
        else
        {
            try
            {
                if ( !item.delete( true, eventMetadata ) )
                {
                    throw new TransferException( "Failed to delete: {}.", item );
                }
            }
            catch ( final IOException e )
            {
                throw new TransferException( "Failed to delete stored location: {}. Reason: {}", e, item,
                                             e.getMessage() );
            }
        }

        contentLogger.info( "FINISH: Delete: {}", item.getResource() );

        return true;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final ConcreteResource resource, final InputStream stream, final long length )
            throws TransferException
    {
        return publish( resource, stream, length, new EventMetadata(  ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final ConcreteResource resource, final InputStream stream, final long length, final EventMetadata eventMetadata )
        throws TransferException
    {
        return publish( resource, stream, length, null, eventMetadata );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long, java.lang.String)
     */
    @Override
    public boolean publish( final ConcreteResource resource, final InputStream stream, final long length,
                            final String contentType)
            throws TransferException
    {
        return publish( resource, stream, length, contentType, new EventMetadata(  ) );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long, java.lang.String)
     */
    @Override
    public boolean publish( final ConcreteResource resource, final InputStream stream, final long length,
                            final String contentType, final EventMetadata metadata )
        throws TransferException
    {
        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( resource, metadata.getPackageType() );
        if ( specialPathInfo != null && !specialPathInfo.isPublishable() )
        {
            throw new TransferException( "Publishing not allowed for: %s", resource );
        }

        return uploader.upload( resource, stream, length, contentType, getTimeoutSeconds( resource ),
                                getTransport( resource ) );
    }

    @Override
    public <T extends TransferBatch> T batchRetrieve( final T batch )
        throws TransferException
    {
        return batchRetrieve( batch, new EventMetadata() );
    }

    @Override
    public <T extends TransferBatch> T batchRetrieve( final T batch , final EventMetadata eventMetadata  )
        throws TransferException
    {
        return doBatch( batch.getResources(), batch, true, eventMetadata );
    }

    @Override
    public <T extends TransferBatch> T batchRetrieveAll( final T batch )
        throws TransferException
    {
        return batchRetrieveAll( batch, new EventMetadata() );
    }

    @Override
    public <T extends TransferBatch> T batchRetrieveAll( final T batch , final EventMetadata eventMetadata  )
        throws TransferException
    {
        final Set<Resource> resources = batch.getResources();
        for ( final Resource resource : new HashSet<>( resources ) )
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

        return doBatch( resources, batch, false, eventMetadata );
    }

    @SuppressWarnings( "RedundantThrows" )
    private <T extends TransferBatch> T doBatch( final Set<Resource> resources, final T batch,
                                                 final boolean suppressFailures, final EventMetadata eventMetadata )
        throws TransferException
    {
        logger.info( "Attempting to batch-retrieve {} resources:\n  {}", resources.size(), new JoinString( "\n  ",
                                                                                                           resources ) );

        final Set<BatchRetriever> retrievers = new HashSet<>( resources.size() );
        for ( final Resource resource : resources )
        {
            retrievers.add( new BatchRetriever( this, resource, suppressFailures, eventMetadata ) );
        }

        final Map<ConcreteResource, TransferException> errors = new HashMap<>();
        final Map<ConcreteResource, Transfer> transfers = new HashMap<>();

        do
        {
            for ( final BatchRetriever retriever : retrievers )
            {
                batchExecutor.submit( retriever );
            }

            int count = retrievers.size();
            for ( int i = 0; i < count; i++ )
            {
                try
                {
                    Future<BatchRetriever> pending = batchExecutor.take();
                    BatchRetriever retriever = pending.get();

                    final ConcreteResource resource = retriever.getLastTry();
                    final TransferException error = retriever.getError();
                    if ( error != null )
                    {
                        logger.warn( String.format( "ERROR: %s...%s", resource, error.getMessage() ), error );
                        retrievers.remove( retriever );

                        if ( !( error instanceof TransferLocationException ) )
                        {
                            errors.put( resource, error );
                        }

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
                catch ( final InterruptedException e )
                {
                    logger.error( String.format( "Failed to wait for batch retrieval attempts to complete: %s",
                                                 e.getMessage() ), e );
                    break;
                }
                catch ( ExecutionException e )
                {
                    logger.error( String.format( "Failed to retrieve next completed retrieval: %s", e.getMessage() ),
                                  e );
                }
            }
        }
        while( !retrievers.isEmpty() );

        batch.setErrors( errors );
        batch.setTransfers( transfers );

        return batch;
    }

}
