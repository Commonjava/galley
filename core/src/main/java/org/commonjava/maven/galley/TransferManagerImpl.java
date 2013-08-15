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
import static org.apache.commons.lang.StringUtils.isEmpty;

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
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.util.ArtifactPathInfo;
import org.commonjava.maven.galley.util.UrlUtils;
import org.commonjava.util.logging.Logger;

public class TransferManagerImpl
    implements TransferManager
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private CacheProvider cacheProvider;

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

    public TransferManagerImpl( final TransportManager transportManager, final CacheProvider cacheProvider,
                                final FileEventManager fileEventManager, final TransferDecorator transferDecorator,
                                final ExecutorService executor )
    {
        this.transportManager = transportManager;
        this.cacheProvider = cacheProvider;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.executor = executor;
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

            target = retrieve( store, path, true );
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
            stream = retrieve( store, path, true );
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
    public Transfer retrieve( final Location store, final String path )
        throws TransferException
    {
        return retrieve( store, path, false );
    }

    private Transfer retrieve( final Location store, final String path, final boolean suppressFailures )
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
            target = getCacheReference( store, path );

            final Transfer retrieved = download( store, target, suppressFailures );

            if ( retrieved != null && retrieved.exists() && !target.equals( retrieved ) )
            {
                cacheProvider.createAlias( target.getLocation(), target.getPath(), retrieved.getLocation(),
                                           retrieved.getPath() );
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
            final TransferException error =
                new TransferException( "Failed to download: %s from: %s. Reason: %s", e, path, store, e.getMessage() );

            fileEventManager.fire( new FileErrorEvent( target, error ) );
            throw error;
        }
    }

    private Transfer download( final Location repository, final Transfer target, final boolean suppressFailures )
        throws TransferException
    {
        if ( !repository.allowsDownloading() )
        {
            return null;
        }

        final String url = buildUrl( repository, target.getPath(), suppressFailures );
        if ( url == null )
        {
            return null;
        }

        //        if ( nfc.hasEntry( url ) )
        //        {
        //            fileEventManager.fire( new FileNotFoundEvent( repository, target.getPath() ) );
        //            return false;
        //        }

        int timeoutSeconds = repository.getTimeoutSeconds();
        if ( timeoutSeconds < 1 )
        {
            timeoutSeconds = Location.DEFAULT_TIMEOUT_SECONDS;
        }

        Transfer result = joinDownload( url, target, timeoutSeconds, suppressFailures );
        if ( result == null )
        {
            result = startDownload( url, repository, target, timeoutSeconds, suppressFailures );
        }

        return result;
    }

    private Transfer startDownload( final String url, final Location repository, final Transfer target,
                                    final int timeoutSeconds, final boolean suppressFailures )
        throws TransferException
    {
        if ( target.exists() )
        {
            return target;
        }

        final String key = getJoinKey( url, false );
        final Transport transport = transportManager.getTransport( repository );
        final DownloadJob job = transport.createDownloadJob( url, repository, target, timeoutSeconds );

        final Future<Transfer> future = executor.submit( job );
        pending.put( key, future );
        try
        {
            final Transfer downloaded = future.get( timeoutSeconds, TimeUnit.SECONDS );

            if ( !suppressFailures && job.getError() != null )
            {
                throw job.getError();
            }

            return downloaded;
        }
        catch ( final InterruptedException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Interrupted download: %s from: %s. Reason: %s", e, url, repository,
                                             e.getMessage() );
            }
        }
        catch ( final ExecutionException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed to download: %s from: %s. Reason: %s", e, url, repository,
                                             e.getMessage() );
            }
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: %s from: %s. Reason: %s", e, url, repository,
                                             e.getMessage() );
            }
        }
        finally
        {
            //            logger.info( "Marking download complete: %s", url );
            pending.remove( key );
        }

        return null;
    }

    private String buildUrl( final Location repository, final String path, final boolean suppressFailures )
        throws TransferException
    {
        final String remoteBase = repository.getUri();
        if ( remoteBase == null )
        {
            return null;
        }

        String url = null;
        try
        {
            url = UrlUtils.buildUrl( remoteBase, path );
        }
        catch ( final MalformedURLException e )
        {
            throw new TransferException( "Invalid URL for path: %s in remote URL: %s. Reason: %s", e, path, remoteBase,
                                         e.getMessage() );
        }

        return url;
    }

    private Transfer joinDownload( final String url, final Transfer target, final int timeoutSeconds,
                                   final boolean suppressFailures )
        throws TransferException
    {
        // if the target file already exists, skip joining.
        if ( target.exists() )
        {
            return target;
        }
        else
        {
            final String key = getJoinKey( url, false );

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

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#store(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream)
     */
    @Override
    public Transfer store( final Location deploy, final String path, final InputStream stream )
        throws TransferException
    {
        if ( !deploy.allowsStoring() )
        {
            throw new TransferException( "Storing not allowed in: %s", deploy );
        }

        final ArtifactPathInfo pathInfo = ArtifactPathInfo.parse( path );
        if ( pathInfo != null && pathInfo.isSnapshot() )
        {
            if ( !deploy.allowsSnapshots() )
            {
                throw new TransferException( "Cannot store snapshot in non-snapshot deploy point: %s", deploy.getUri() );
            }
        }
        else if ( !deploy.allowsReleases() )
        {
            throw new TransferException( "Cannot store release in snapshot-only deploy point: %s", deploy.getUri() );
        }

        final Transfer target = getCacheReference( deploy, path );

        OutputStream out = null;
        try
        {
            out = target.openOutputStream( TransferOperation.UPLOAD );
            copy( stream, out );
        }
        catch ( final IOException e )
        {
            throw new TransferException( "Failed to store: %s in: %s. Reason: %s", e, path, deploy.getUri(),
                                         e.getMessage() );
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
        final ArtifactPathInfo pathInfo = ArtifactPathInfo.parse( path );

        Location selected = null;
        for ( final Location store : stores )
        {
            if ( !store.allowsStoring() )
            {
                continue;
            }

            //                logger.info( "Found deploy point: %s", store.getName() );
            if ( pathInfo == null )
            {
                // probably not an artifact, most likely metadata instead...
                //                    logger.info( "Selecting it for non-artifact storage: %s", path );
                selected = store;
                break;
            }
            else if ( pathInfo.isSnapshot() )
            {
                if ( store.allowsSnapshots() )
                {
                    //                        logger.info( "Selecting it for snapshot storage: %s", pathInfo );
                    selected = store;
                    break;
                }
            }
            else if ( store.allowsReleases() )
            {
                //                    logger.info( "Selecting it for release storage: %s", pathInfo );
                selected = store;
                break;
            }
        }

        if ( selected == null )
        {
            logger.warn( "Cannot deploy. No valid deploy points in group." );
            throw new TransferException( "No deployment locations available for: %s in: %s", path, stores );
        }

        store( selected, path, stream );

        return getCacheReference( selected, path );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#parsePathInfo(java.lang.String)
     */
    @Override
    public ArtifactPathInfo parsePathInfo( final String path )
    {
        if ( isEmpty( path ) || path.endsWith( "/" ) )
        {
            return null;
        }

        final String[] parts = path.split( "/" );
        if ( parts.length < 4 )
        {
            return null;
        }

        final String file = parts[parts.length - 1];
        final String version = parts[parts.length - 2];
        final String artifactId = parts[parts.length - 3];
        final StringBuilder groupId = new StringBuilder();
        for ( int i = 0; i < parts.length - 3; i++ )
        {
            if ( groupId.length() > 0 )
            {
                groupId.append( '.' );
            }

            groupId.append( parts[i] );
        }

        return new ArtifactPathInfo( groupId.toString(), artifactId, version, file, path );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#getStoreRootDirectory(org.commonjava.maven.galley.model.Location)
     */
    @Override
    public Transfer getStoreRootDirectory( final Location key )
    {
        return new Transfer( key, cacheProvider, fileEventManager, transferDecorator, Transfer.ROOT );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#getCacheReference(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public Transfer getCacheReference( final Location store, final String... path )
    {
        return new Transfer( store, cacheProvider, fileEventManager, transferDecorator, path );
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
            result = delete( store, path ) || result;
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#delete(org.commonjava.maven.galley.model.Location, java.lang.String)
     */
    @Override
    public boolean delete( final Location store, final String path )
        throws TransferException
    {
        final Transfer item = getCacheReference( store, path == null ? Transfer.ROOT : path );
        return doDelete( item );
    }

    private Boolean doDelete( final Transfer item )
        throws TransferException
    {
        if ( !item.exists() )
        {
            return false;
        }

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
                throw new TransferException( "Failed to delete stored location: %s. Reason: %s", e, item,
                                             e.getMessage() );
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long)
     */
    @Override
    public boolean publish( final Location location, final String path, final InputStream stream, final long length )
        throws TransferException
    {
        return publish( location, path, stream, length, null );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.TransferManager#publish(org.commonjava.maven.galley.model.Location, java.lang.String, java.io.InputStream, long, java.lang.String)
     */
    @Override
    public boolean publish( final Location location, final String path, final InputStream stream, final long length,
                            final String contentType )
        throws TransferException
    {
        if ( !location.allowsPublishing() )
        {
            throw new TransferException( "Publishing not allowed in: %s", location );
        }

        final String url = buildUrl( location, path, false );
        if ( url == null )
        {
            return false;
        }

        int timeoutSeconds = location.getTimeoutSeconds();
        if ( timeoutSeconds < 1 )
        {
            timeoutSeconds = Location.DEFAULT_TIMEOUT_SECONDS;
        }

        joinPublish( url, path, timeoutSeconds );
        return doPublish( url, location, path, timeoutSeconds, stream, length, contentType );
    }

    private boolean doPublish( final String url, final Location repository, final String path,
                               final int timeoutSeconds, final InputStream stream, final long length,
                               final String contentType )
        throws TransferException
    {
        final String key = getJoinKey( url, true );

        final Transport transport = transportManager.getTransport( repository );
        final PublishJob job =
            transport.createPublishJob( url, repository, path, stream, length, contentType, timeoutSeconds );

        final Future<Boolean> future = executor.submit( job );
        pending.put( key, future );
        try
        {
            final Boolean published = future.get( timeoutSeconds, TimeUnit.SECONDS );

            if ( job.getError() != null )
            {
                throw job.getError();
            }

            return published;
        }
        catch ( final InterruptedException e )
        {
            throw new TransferException( "Interrupted publish: %s from: %s. Reason: %s", e, url, repository,
                                         e.getMessage() );
        }
        catch ( final ExecutionException e )
        {
            throw new TransferException( "Failed to publish: %s from: %s. Reason: %s", e, url, repository,
                                         e.getMessage() );
        }
        catch ( final TimeoutException e )
        {
            throw new TransferException( "Timed-out publish: %s from: %s. Reason: %s", e, url, repository,
                                         e.getMessage() );
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
    private boolean joinPublish( final String url, final String path, final int timeoutSeconds )
        throws TransferException
    {
        final String key = getJoinKey( url, true );

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

    private String getJoinKey( final String url, final boolean upload )
    {
        return ( upload ? "UP" : "DOWN" ) + "::" + url;
    }
}
