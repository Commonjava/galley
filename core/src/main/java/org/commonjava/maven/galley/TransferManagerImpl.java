/*******************************************************************************
 * Copyright 2011 John Casey
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.commonjava.maven.galley;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.maven.galley.event.FileErrorEvent;
import org.commonjava.maven.galley.event.FileNotFoundEvent;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.util.UrlUtils;
import org.commonjava.util.logging.Logger;

public class TransferManagerImpl
    implements TransferManager
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private CacheProvider cacheProvider;

    @Inject
    private NotFoundCache nfc;

    @Inject
    private TransportManager transportManager;

    @Inject
    private FileEventManager fileEventManager;

    @Inject
    private TransferDecorator transferDecorator;

    private final Map<String, Future<?>> pending = new ConcurrentHashMap<String, Future<?>>();

    @Inject
    @ExecutorConfig( threads = 4, daemon = true, named = "galley-transfers", priority = 8 )
    private ExecutorService executor;

    protected TransferManagerImpl()
    {
    }

    public TransferManagerImpl( final TransportManager transportManager, final CacheProvider cacheProvider, final NotFoundCache nfc,
                                final FileEventManager fileEventManager, final TransferDecorator transferDecorator, final ExecutorService executor )
    {
        this.transportManager = transportManager;
        this.cacheProvider = cacheProvider;
        this.nfc = nfc;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.executor = executor;
    }

    @Override
    public ListingResult list( final Resource resource )
        throws TransferException
    {
        final Transfer cached = getCacheReference( resource );
        ListingResult cacheResult = null;
        if ( cached.exists() )
        {
            if ( !cached.isDirectory() )
            {
                throw new TransferException( "Cannot list: %s. It does not appear to be a directory." );
            }
            else
            {
                cacheResult = new ListingResult( resource, cached.list() );
            }
        }

        final int timeoutSeconds = getTimeoutSeconds( resource );
        final ListingResult remoteResult = getListing( resource, timeoutSeconds, false );

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

        if ( result == null )
        {
            nfc.addMissing( resource );
        }
        else
        {
            nfc.clearMissing( resource );
        }

        return result;
    }

    private ListingResult getListing( final Resource resource, final int timeoutSeconds, final boolean suppressFailures )
        throws TransferException
    {
        if ( nfc.isMissing( resource ) )
        {
            return null;
        }

        final Transport transport = transportManager.getTransport( resource );
        final String url = buildUrl( resource, suppressFailures );
        logger.info( "LIST %s", url );

        final ListingJob job = transport.createListingJob( url, resource, timeoutSeconds );

        try
        {
            final ListingResult result = job.call();

            if ( !suppressFailures && job.getError() != null )
            {
                throw job.getError();
            }

            return result;
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: %s. Reason: %s", e, resource, e.getMessage() );
            }
        }
        catch ( final Exception e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed listing: %s. Reason: %s", e, resource, e.getMessage() );
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieveFirst(java.util.List, java.lang.String)
     */
    @Override
    public Transfer retrieveFirst( final List<? extends Location> stores, final String path )
        throws TransferException
    {
        Transfer target = null;

        for ( final Location store : stores )
        {
            if ( store == null )
            {
                continue;
            }

            target = retrieve( new Resource( store, path ), true );
            if ( target != null )
            {
                return target;
            }
        }

        fileEventManager.fire( new FileNotFoundEvent( stores, path ) );
        return null;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieveAll(java.util.List, java.lang.String)
     */
    @Override
    public Set<Transfer> retrieveAll( final List<? extends Location> stores, final String path )
        throws TransferException
    {
        final Set<Transfer> results = new LinkedHashSet<Transfer>();

        Transfer stream = null;
        for ( final Location store : stores )
        {
            stream = retrieve( new Resource( store, path ), true );
            if ( stream != null )
            {
                results.add( stream );
            }
        }

        if ( results.isEmpty() )
        {
            fileEventManager.fire( new FileNotFoundEvent( stores, path ) );
        }

        return results;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#retrieve(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public Transfer retrieve( final Resource resource )
        throws TransferException
    {
        return retrieve( resource, false );
    }

    private Transfer retrieve( final Resource resource, final boolean suppressFailures )
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

            final Transfer retrieved = download( resource, target, suppressFailures );

            if ( retrieved != null && retrieved.exists() && !target.equals( retrieved ) )
            {
                cacheProvider.createAlias( retrieved.getResource(), target.getResource() );
            }

            if ( target.exists() )
            {
                return target;
            }
            else
            {
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
            final TransferException error = new TransferException( "Failed to download: %s. Reason: %s", e, resource, e.getMessage() );

            fileEventManager.fire( new FileErrorEvent( target, error ) );
            throw error;
        }
    }

    private Transfer download( final Resource resource, final Transfer target, final boolean suppressFailures )
        throws TransferException
    {
        if ( !resource.allowsDownloading() )
        {
            return null;
        }

        final String url = buildUrl( resource, suppressFailures );
        if ( url == null )
        {
            return null;
        }

        if ( nfc.isMissing( resource ) )
        {
            return null;
        }

        logger.info( "RETRIEVE %s", url );

        //        if ( nfc.hasEntry( url ) )
        //        {
        //            fileEventManager.fire( new FileNotFoundEvent( repository, target.getPath() ) );
        //            return false;
        //        }

        final int timeoutSeconds = getTimeoutSeconds( resource );
        Transfer result = joinDownload( url, target, timeoutSeconds, suppressFailures );
        if ( result == null )
        {
            result = startDownload( url, resource, target, timeoutSeconds, suppressFailures );
        }

        return result;
    }

    private int getTimeoutSeconds( final Resource resource )
    {
        int timeoutSeconds = resource.getTimeoutSeconds();
        if ( timeoutSeconds < 1 )
        {
            timeoutSeconds = Location.DEFAULT_TIMEOUT_SECONDS;
        }

        return timeoutSeconds;
    }

    private Transfer joinDownload( final String url, final Transfer target, final int timeoutSeconds, final boolean suppressFailures )
        throws TransferException
    {
        // if the target file already exists, skip joining.
        if ( target.exists() )
        {
            return target;
        }
        else
        {
            final String key = getJoinKey( url, TransferOperation.DOWNLOAD );

            @SuppressWarnings( "unchecked" )
            final Future<Transfer> future = (Future<Transfer>) pending.get( key );
            if ( future != null )
            {
                Transfer f = null;
                try
                {
                    f = future.get( timeoutSeconds, TimeUnit.SECONDS );

                    return f;
                }
                catch ( final InterruptedException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Download interrupted: %s", e, url );
                    }
                }
                catch ( final ExecutionException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Download failed: %s", e, url );
                    }
                }
                catch ( final TimeoutException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Timeout on: %s", e, url );
                    }
                }
            }
        }

        return null;
    }

    private Transfer startDownload( final String url, final Resource resource, final Transfer target, final int timeoutSeconds,
                                    final boolean suppressFailures )
        throws TransferException
    {
        if ( target.exists() )
        {
            return target;
        }

        final String key = getJoinKey( url, TransferOperation.DOWNLOAD );
        final Transport transport = transportManager.getTransport( resource );
        final DownloadJob job = transport.createDownloadJob( url, resource, target, timeoutSeconds );

        final Future<Transfer> future = executor.submit( job );
        pending.put( key, future );
        try
        {
            final Transfer downloaded = future.get( timeoutSeconds, TimeUnit.SECONDS );

            if ( !suppressFailures && job.getError() != null )
            {
                nfc.addMissing( resource );
                throw job.getError();
            }

            nfc.clearMissing( resource );
            return downloaded;
        }
        catch ( final InterruptedException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Interrupted download: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
            }
        }
        catch ( final ExecutionException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed to download: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
            }
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
            }
        }
        finally
        {
            //            logger.info( "Marking download complete: %s", url );
            pending.remove( key );
        }

        return null;
    }

    private String buildUrl( final Resource resource, final boolean suppressFailures )
        throws TransferException
    {
        final String remoteBase = resource.getLocationUri();
        if ( remoteBase == null )
        {
            return null;
        }

        String url = null;
        try
        {
            url = UrlUtils.buildUrl( remoteBase, resource.getPath() );
        }
        catch ( final MalformedURLException e )
        {
            throw new TransferException( "Invalid URL for: %s. Reason: %s", e, resource, e.getMessage() );
        }

        return url;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#store(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream)
     */
    @Override
    public Transfer store( final Resource resource, final InputStream stream )
        throws TransferException
    {
        if ( !resource.allowsStoring() )
        {
            throw new TransferException( "Storing not allowed in: %s", resource );
        }

        ArtifactRules.checkStorageAuthorization( resource );
        final Transfer target = getCacheReference( resource );

        logger.info( "STORE %s", target.getResource() );

        OutputStream out = null;
        try
        {
            out = target.openOutputStream( TransferOperation.UPLOAD );
            copy( stream, out );

            nfc.clearMissing( resource );
        }
        catch ( final IOException e )
        {
            throw new TransferException( "Failed to store: %s. Reason: %s", e, resource, e.getMessage() );
        }
        finally
        {
            closeQuietly( out );
        }

        return target;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#store(java.util.List, java.lang.String, java.io.InputStream)
     */
    @Override
    public Transfer store( final List<? extends Location> stores, final String path, final InputStream stream )
        throws TransferException
    {
        final Location selected = ArtifactRules.selectStorageLocation( path, stores );

        if ( selected == null )
        {
            logger.warn( "Cannot deploy. No valid deploy points in group." );
            throw new TransferException( "No deployment locations available for: %s in: %s", path, stores );
        }

        final Resource res = new Resource( selected, path );
        store( res, stream );

        return getCacheReference( res );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#getStoreRootDirectory(org.commonjava.maven.galley.model.Location)
     */
    @Override
    public Transfer getStoreRootDirectory( final Location key )
    {
        return new Transfer( new Resource( key ), cacheProvider, fileEventManager, transferDecorator );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#getCacheReference(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public Transfer getCacheReference( final Resource resource )
    {
        return new Transfer( resource, cacheProvider, fileEventManager, transferDecorator );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#deleteAll(java.util.List, java.lang.String)
     */
    @Override
    public boolean deleteAll( final List<? extends Location> stores, final String path )
        throws TransferException
    {
        boolean result = false;
        for ( final Location store : stores )
        {
            result = delete( new Resource( store, path ) ) || result;
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#delete(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public boolean delete( final Resource resource )
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

        logger.info( "DELETE %s", item.getResource() );

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
                    throw new TransferException( "Failed to delete: %s.", item );
                }
            }
            catch ( final IOException e )
            {
                throw new TransferException( "Failed to delete stored location: %s. Reason: %s", e, item, e.getMessage() );
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final Resource resource, final InputStream stream, final long length )
        throws TransferException
    {
        return publish( resource, stream, length, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long, java.lang.String)
     */
    @Override
    public boolean publish( final Resource resource, final InputStream stream, final long length, final String contentType )
        throws TransferException
    {
        if ( !resource.allowsPublishing() )
        {
            throw new TransferException( "Publishing not allowed in: %s", resource );
        }

        final String url = buildUrl( resource, false );
        if ( url == null )
        {
            return false;
        }

        logger.info( "PUBLISH %s", url );

        int timeoutSeconds = resource.getTimeoutSeconds();
        if ( timeoutSeconds < 1 )
        {
            timeoutSeconds = Location.DEFAULT_TIMEOUT_SECONDS;
        }

        joinPublish( url, resource, timeoutSeconds );
        return doPublish( url, resource, timeoutSeconds, stream, length, contentType );
    }

    private boolean doPublish( final String url, final Resource resource, final int timeoutSeconds, final InputStream stream, final long length,
                               final String contentType )
        throws TransferException
    {
        final String key = getJoinKey( url, TransferOperation.UPLOAD );

        final Transport transport = transportManager.getTransport( resource );
        final PublishJob job = transport.createPublishJob( url, resource, stream, length, contentType, timeoutSeconds );

        final Future<Boolean> future = executor.submit( job );
        pending.put( key, future );
        try
        {
            final Boolean published = future.get( timeoutSeconds, TimeUnit.SECONDS );

            if ( job.getError() != null )
            {
                throw job.getError();
            }

            nfc.clearMissing( resource );
            return published;
        }
        catch ( final InterruptedException e )
        {
            throw new TransferException( "Interrupted publish: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
        }
        catch ( final ExecutionException e )
        {
            throw new TransferException( "Failed to publish: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
        }
        catch ( final TimeoutException e )
        {
            throw new TransferException( "Timed-out publish: %s from: %s. Reason: %s", e, url, resource, e.getMessage() );
        }
        finally
        {
            //            logger.info( "Marking download complete: %s", url );
            pending.remove( key );
        }
    }

    /**
     * @return true if (a) previous upload in progress succeeded, or (b) there was no previous upload.
     */
    private boolean joinPublish( final String url, final Resource resource, final int timeoutSeconds )
        throws TransferException
    {
        final String key = getJoinKey( url, TransferOperation.UPLOAD );

        @SuppressWarnings( "unchecked" )
        final Future<Boolean> future = (Future<Boolean>) pending.get( key );
        if ( future != null )
        {
            Boolean f = null;
            try
            {
                f = future.get( timeoutSeconds, TimeUnit.SECONDS );

                return f;
            }
            catch ( final InterruptedException e )
            {
                throw new TransferException( "Publish interrupted: %s", e, url );
            }
            catch ( final ExecutionException e )
            {
                throw new TransferException( "Publish failed: %s", e, url );
            }
            catch ( final TimeoutException e )
            {
                throw new TransferException( "Timeout on: %s", e, url );
            }
        }

        return true;
    }

    private String getJoinKey( final String url, final TransferOperation operation )
    {
        return operation.name() + "::" + url;
    }

}
