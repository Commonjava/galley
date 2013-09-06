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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.commonjava.maven.galley.event.FileErrorEvent;
import org.commonjava.maven.galley.event.FileNotFoundEvent;
import org.commonjava.maven.galley.internal.xfer.DownloadHandler;
import org.commonjava.maven.galley.internal.xfer.ExistenceHandler;
import org.commonjava.maven.galley.internal.xfer.ListingHandler;
import org.commonjava.maven.galley.internal.xfer.UploadHandler;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;
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

    @Inject
    private DownloadHandler downloader;

    @Inject
    private UploadHandler uploader;

    @Inject
    private ListingHandler lister;

    @Inject
    private ExistenceHandler exister;

    protected TransferManagerImpl()
    {
    }

    public TransferManagerImpl( final TransportManager transportManager, final CacheProvider cacheProvider, final NotFoundCache nfc,
                                final FileEventManager fileEventManager, final TransferDecorator transferDecorator, final DownloadHandler downloader,
                                final UploadHandler uploader, final ListingHandler lister, final ExistenceHandler exister )
    {
        this.transportManager = transportManager;
        this.cacheProvider = cacheProvider;
        this.nfc = nfc;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.downloader = downloader;
        this.uploader = uploader;
        this.lister = lister;
        this.exister = exister;
    }

    @Override
    public boolean exists( final Resource resource )
        throws TransferException
    {
        return exists( resource, false );
    }

    @Override
    public Resource findFirstExisting( final List<? extends Location> locations, final String path )
        throws TransferException
    {
        for ( final Location location : locations )
        {
            final Resource resource = new Resource( location, path );
            if ( exists( resource, true ) )
            {
                return resource;
            }
        }

        return null;
    }

    @Override
    public List<Resource> findAllExisting( final List<? extends Location> locations, final String path )
        throws TransferException
    {
        final List<Resource> results = new ArrayList<>();
        for ( final Location location : locations )
        {
            final Resource resource = new Resource( location, path );
            if ( exists( resource, true ) )
            {
                results.add( resource );
            }
        }

        return results;
    }

    private boolean exists( final Resource resource, final boolean suppressFailures )
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
    public List<ListingResult> listAll( final List<? extends Location> locations, final String path )
        throws TransferException
    {
        final List<ListingResult> results = new ArrayList<>();
        for ( final Location location : locations )
        {
            final ListingResult result = doList( new Resource( location, path ), true );
            if ( result != null )
            {
                results.add( result );
            }
        }

        return results;
    }

    @Override
    public ListingResult list( final Resource resource )
        throws TransferException
    {
        return doList( resource, false );
    }

    private ListingResult doList( final Resource resource, final boolean suppressFailures )
        throws TransferException
    {
        final Transfer cached = getCacheReference( resource );
        ListingResult cacheResult = null;
        if ( cached.exists() )
        {
            if ( !cached.isDirectory() )
            {
                throw new TransferException( "Cannot list: %s. It does not appear to be a directory.", resource );
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

    private Transport getTransport( final Resource resource )
        throws TransferException
    {
        final Transport transport = transportManager.getTransport( resource );
        if ( transport == null )
        {
            if ( resource.getLocationUri() == null )
            {
                logger.info( "NFC: No remote URI. Marking as missing: %s", resource );
                nfc.addMissing( resource );
                return null;
            }

            throw new TransferException( "No transports available to handle: %s", resource );
        }

        return transport;
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
    public List<Transfer> retrieveAll( final List<? extends Location> stores, final String path )
        throws TransferException
    {
        final List<Transfer> results = new ArrayList<Transfer>();

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

            final Transfer retrieved =
                downloader.download( resource, target, getTimeoutSeconds( resource ), getTransport( resource ), suppressFailures );

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
        return uploader.upload( resource, stream, length, contentType, getTimeoutSeconds( resource ), getTransport( resource ) );
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

}
