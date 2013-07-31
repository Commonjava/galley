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

import javax.xml.ws.Response;

import org.commonjava.maven.galley.cache.CacheProvider;
import org.commonjava.maven.galley.htcli.Downloader;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.tansport.TansportManager;
import org.commonjava.util.logging.Logger;

public class TransferManager
{

    private static final String ROOT_PATH = "/";

    private final Logger logger = new Logger( getClass() );

    private CacheProvider storage;

    private TansportManager transportManager;

    private final Map<String, Future<Transfer>> pending = new ConcurrentHashMap<String, Future<Transfer>>();

    //    @ExecutorConfig( priority = 10, threads = 2, named = "file-manager" )
    private ExecutorService executor; // = Executors.newFixedThreadPool( 8 );

    public TransferManager()
    {
    }

    //    public TransferManager( final AproxConfiguration config, final StorageProvider storage, final AproxHttp http/*,
    //                                                                                                                   final NotFoundCache nfc*/)
    //    {
    //        this.config = config;
    //        this.storage = storage;
    //        this.http = http;
    //        //        this.nfc = nfc;
    //        this.fileEventManager = new FileEventManager();
    //        executor = Executors.newFixedThreadPool( 10 );
    //    }

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

        //        fileEventManager.fire( new FileNotFoundEvent( stores, path ) );
        return null;
    }

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

        //        if ( results.isEmpty() )
        //        {
        //            fileEventManager.fire( new FileNotFoundEvent( stores, path ) );
        //        }

        return results;
    }

    /*
     * (non-Javadoc)
     * @see org.commonjava.aprox.core.rest.util.FileManager#download(org.commonjava.aprox.core.model.Location,
     * java.lang.String)
     */

    public Transfer retrieve( final Location store, final String path )
        throws TransferException
    {
        return retrieve( store, path, false );
    }

    private Transfer retrieve( final Location store, final String path, final boolean suppressFailures )
        throws TransferException
    {
        Transfer target = null;
        try
        {
            if ( store instanceof Repository )
            {
                final Repository repo = (Repository) store;
                target = getStorageReference( store, path );

                download( repo, target, suppressFailures );
            }
            else
            {
                target = getStorageReference( store, path );
            }

            if ( target.exists() )
            {
                //                logger.info( "Using stored copy from artifact store: %s for: %s", store.getName(), path );
                final Transfer item = getStorageReference( store.getKey(), path );

                return item;
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
    }

    /*
     * (non-Javadoc)
     * @see org.commonjava.aprox.core.rest.util.FileManager#download(org.commonjava.aprox.core.model.Repository,
     * java.lang.String, java.io.File, boolean)
     */
    private boolean download( final Repository repository, final Transfer target, final boolean suppressFailures )
        throws TransferException
    {
        final String url = buildDownloadUrl( repository, target.getPath(), suppressFailures );

        //        if ( nfc.hasEntry( url ) )
        //        {
        //            fileEventManager.fire( new FileNotFoundEvent( repository, target.getPath() ) );
        //            return false;
        //        }

        int timeoutSeconds = repository.getTimeoutSeconds();
        if ( timeoutSeconds < 1 )
        {
            timeoutSeconds = Repository.DEFAULT_TIMEOUT_SECONDS;
        }

        if ( !joinDownload( url, target, timeoutSeconds, suppressFailures ) )
        {
            startDownload( url, repository, target, timeoutSeconds, suppressFailures );
        }

        return target.exists();
    }

    private boolean startDownload( final String url, final Repository repository, final Transfer target,
                                   final int timeoutSeconds, final boolean suppressFailures )
        throws TransferException
    {
        final Downloader dl = new Downloader( /**nfc,**/
        url, repository, target, http );

        final Future<Transfer> future = executor.submit( dl );
        pending.put( url, future );

        boolean result = true;
        try
        {
            final Transfer downloaded = future.get( timeoutSeconds, TimeUnit.SECONDS );

            if ( !suppressFailures && dl.getError() != null )
            {
                throw dl.getError();
            }

            result = downloaded != null && downloaded.exists();
        }
        catch ( final InterruptedException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( Response.status( Status.NO_CONTENT )
                                                     .build(), "Interrupted download: %s from: %s. Reason: %s", e, url,
                                             repository, e.getMessage() );
            }
            result = false;
        }
        catch ( final ExecutionException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( Response.serverError()
                                                     .build(), "Failed to download: %s from: %s. Reason: %s", e, url,
                                             repository, e.getMessage() );
            }
            result = false;
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( Response.status( Status.NO_CONTENT )
                                                     .build(), "Timed-out download: %s from: %s. Reason: %s", e, url,
                                             repository, e.getMessage() );
            }
            result = false;
        }
        finally
        {
            //            logger.info( "Marking download complete: %s", url );
            pending.remove( url );
        }

        return result;
    }

    private String buildDownloadUrl( final Repository repository, final String path, final boolean suppressFailures )
        throws TransferException
    {
        final String remoteBase = repository.getUrl();
        String url = null;
        try
        {
            url = buildUrl( remoteBase, path );
        }
        catch ( final MalformedURLException e )
        {
            logger.error( "Invalid URL for path: %s in remote URL: %s. Reason: %s", e, path, remoteBase, e.getMessage() );

            if ( !suppressFailures )
            {
                throw new TransferException( Response.status( Status.BAD_REQUEST )
                                                     .build() );
            }
            else
            {
                url = null;
            }
        }

        return url;
    }

    private boolean joinDownload( final String url, final Transfer target, final int timeoutSeconds,
                                  final boolean suppressFailures )
        throws TransferException
    {
        boolean result = target.exists();

        // if the target file already exists, skip joining.
        if ( !result )
        {
            final Future<Transfer> future = pending.get( url );
            if ( future != null )
            {
                Transfer f = null;
                try
                {
                    f = future.get( timeoutSeconds, TimeUnit.SECONDS );

                    // if the download landed in a different repository, copy it to the current one for
                    // completeness...

                    // NOTE: It'd be nice to alias instead of copying, but
                    // that would require a common centralized store
                    // to prevent removal of a repository from hosing
                    // the links.
                    if ( f != null && f.exists() && !f.equals( target ) )
                    {
                        target.copyFrom( f );
                    }

                    result = target != null && target.exists();
                }
                catch ( final InterruptedException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( Response.status( Status.NO_CONTENT )
                                                             .build() );
                    }
                }
                catch ( final ExecutionException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( Response.serverError()
                                                             .build() );
                    }
                }
                catch ( final TimeoutException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( Response.status( Status.NO_CONTENT )
                                                             .build() );
                    }
                }
                catch ( final IOException e )
                {
                    logger.error( "Failed to copy downloaded file to repository target. Error:  %s\nDownloaded location: %s\nRepository target: %s",
                                  e, e.getMessage(), f, target );

                    if ( !suppressFailures )
                    {
                        throw new TransferException( Response.serverError()
                                                             .build() );
                    }
                }
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.commonjava.aprox.core.rest.util.FileManager#upload(org.commonjava.aprox.core.model.DeployPoint,
     * java.lang.String, java.io.InputStream)
     */

    public Transfer store( final DeployPoint deploy, final String path, final InputStream stream )
        throws TransferException
    {
        final ArtifactPathInfo pathInfo = ArtifactPathInfo.parse( path );
        if ( pathInfo != null && pathInfo.isSnapshot() )
        {
            if ( !deploy.isAllowSnapshots() )
            {
                logger.error( "Cannot store snapshot in non-snapshot deploy point: %s", deploy.getName() );
                throw new TransferException( Response.status( Status.BAD_REQUEST )
                                                     .build() );
            }
        }
        else if ( !deploy.isAllowReleases() )
        {
            logger.error( "Cannot store release in snapshot-only deploy point: %s", deploy.getName() );
            throw new TransferException( Response.status( Status.BAD_REQUEST )
                                                 .build() );
        }

        final Transfer target = getStorageReference( deploy, path );

        // TODO: Need some protection for released files!
        // if ( target.exists() )
        // {
        // throw new WebApplicationException(
        // Response.status( Status.BAD_REQUEST ).entity( "Deployment path already exists." ).build() );
        // }

        OutputStream out = null;
        try
        {
            out = target.openOutputStream( false );
            copy( stream, out );
        }
        catch ( final IOException e )
        {
            logger.error( "Failed to store: %s in deploy store: %s. Reason: %s", e, path, deploy.getName(),
                          e.getMessage() );

            throw new TransferException( Response.serverError()
                                                 .build() );
        }
        finally
        {
            closeQuietly( out );
        }

        return target;
    }

    /*
     * (non-Javadoc)
     * @see org.commonjava.aprox.core.rest.util.FileManager#upload(java.util.List, java.lang.String,
     * java.io.InputStream)
     */

    public Transfer store( final List<? extends Location> stores, final String path, final InputStream stream )
        throws TransferException
    {
        final ArtifactPathInfo pathInfo = ArtifactPathInfo.parse( path );

        DeployPoint selected = null;
        for ( final Location store : stores )
        {
            if ( store instanceof DeployPoint )
            {
                //                logger.info( "Found deploy point: %s", store.getName() );
                final DeployPoint dp = (DeployPoint) store;
                if ( pathInfo == null )
                {
                    // probably not an artifact, most likely metadata instead...
                    //                    logger.info( "Selecting it for non-artifact storage: %s", path );
                    selected = dp;
                    break;
                }
                else if ( pathInfo.isSnapshot() )
                {
                    if ( dp.isAllowSnapshots() )
                    {
                        //                        logger.info( "Selecting it for snapshot storage: %s", pathInfo );
                        selected = dp;
                        break;
                    }
                }
                else if ( dp.isAllowReleases() )
                {
                    //                    logger.info( "Selecting it for release storage: %s", pathInfo );
                    selected = dp;
                    break;
                }
            }
        }

        if ( selected == null )
        {
            logger.warn( "Cannot deploy. No valid deploy points in group." );
            throw new TransferException( Response.status( Status.BAD_REQUEST )
                                                 .entity( "No deployment locations available." )
                                                 .build() );
        }

        store( selected, path, stream );

        return getStorageReference( selected.getKey(), path );
    }

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

    public Transfer getStoreRootDirectory( final StoreKey key )
    {
        return new Transfer( key, storage, fileEventManager, Transfer.ROOT );
    }

    public Transfer getStorageReference( final Location store, final String... path )
    {
        return new Transfer( store.getKey(), storage, fileEventManager, path );
    }

    public Transfer getStorageReference( final StoreKey key, final String... path )
    {
        return new Transfer( key, storage, fileEventManager, path );
    }

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

    public boolean delete( final Location store, final String path )
        throws TransferException
    {
        final Transfer item = getStorageReference( store, path == null ? ROOT_PATH : path );
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

}
