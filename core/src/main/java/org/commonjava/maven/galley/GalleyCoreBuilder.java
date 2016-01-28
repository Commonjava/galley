/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.commonjava.cdi.util.weft.NamedThreadFactory;
import org.commonjava.maven.galley.auth.MemoryPasswordManager;
import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.internal.TransferManagerImpl;
import org.commonjava.maven.galley.internal.xfer.DownloadHandler;
import org.commonjava.maven.galley.internal.xfer.ExistenceHandler;
import org.commonjava.maven.galley.internal.xfer.ListingHandler;
import org.commonjava.maven.galley.internal.xfer.UploadHandler;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.io.SpecialPathManagerImpl;
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.maven.galley.transport.SimpleUrlLocationResolver;
import org.commonjava.maven.galley.transport.TransportManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GalleyCoreBuilder
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private LocationExpander locationExpander;

    private LocationResolver locationResolver;

    private TransferDecorator decorator;

    private FileEventManager events;

    private CacheProvider cache;

    private NotFoundCache nfc;

    private TransportManager transportManager;

    private TransferManager transferManager;

    private List<Transport> transports;

    private SpecialPathManager specialPathManager;

    private ExecutorService handlerExecutor;

    private ExecutorService batchExecutor;

    private File cacheDir;

    private PasswordManager passwordManager;

    public GalleyCoreBuilder( final CacheProvider cache )
    {
        this.cache = cache;
    }

    public GalleyCoreBuilder( final File cacheDir )
    {
        this.cacheDir = cacheDir;
    }

    public GalleyCore build()
        throws GalleyInitException
    {
        initMissingComponents();
        return new GalleyCore( locationExpander, locationResolver, decorator, events, cache, nfc, transportManager,
                               transferManager, transports, handlerExecutor, batchExecutor, passwordManager );
    }

    public void initMissingComponents()
        throws GalleyInitException
    {
        if ( transportManager == null )
        {
            transportManager = new TransportManagerImpl( transports );
        }

        handlerExecutor = Executors.newFixedThreadPool( 2, new NamedThreadFactory( "transfer-handlers", true, 4 ) );
        batchExecutor = Executors.newFixedThreadPool( 2, new NamedThreadFactory( "transfer-batches", true, 4 ) );

        if ( decorator == null )
        {
            decorator = new NoOpTransferDecorator();
        }

        if ( events == null )
        {
            events = new NoOpFileEventManager();
        }

        if ( cache == null )
        {
            if ( cacheDir != null )
            {
                cache = new FileCacheProvider( cacheDir, new HashedLocationPathGenerator(), events, decorator );
            }
            else
            {
                throw new GalleyInitException(
                                               "No CacheProvider or cache directory supplied before calling initMissingComponents()!" );
            }
        }

        if ( nfc == null )
        {
            nfc = new MemoryNotFoundCache();
        }
        
        final DownloadHandler dh = new DownloadHandler( getNfc(), handlerExecutor );
        final UploadHandler uh = new UploadHandler( getNfc(), handlerExecutor );
        final ListingHandler lh = new ListingHandler( getNfc() );
        final ExistenceHandler eh = new ExistenceHandler( getNfc() );

        if ( specialPathManager == null )
        {
            specialPathManager = new SpecialPathManagerImpl();
        }

        if ( transferManager == null )
        {
            transferManager =
                new TransferManagerImpl( transportManager, getCache(), getNfc(), getFileEvents(), dh, uh, lh, eh, specialPathManager,
                                         batchExecutor );
        }

        if ( locationExpander == null )
        {
            logger.debug( "Initializing default location expander" );
            locationExpander = new NoOpLocationExpander();
        }

        if ( locationResolver == null )
        {
            locationResolver = new SimpleUrlLocationResolver( locationExpander, transportManager );
        }

        if ( passwordManager == null )
        {
            passwordManager = new MemoryPasswordManager();
        }
    }

    public PasswordManager getPasswordManager()
    {
        return passwordManager;
    }

    public GalleyCoreBuilder withPasswordManager( final PasswordManager passwordManager )
    {
        this.passwordManager = passwordManager;
        return this;
    }

    public LocationExpander getLocationExpander()
    {
        return locationExpander;
    }

    public TransferDecorator getTransferDecorator()
    {
        return decorator;
    }

    public FileEventManager getFileEvents()
    {
        return events;
    }

    public CacheProvider getCache()
    {
        return cache;
    }

    public NotFoundCache getNfc()
    {
        return nfc;
    }

    public SpecialPathManager getSpecialPathManager()
    {
        return specialPathManager;
    }

    public GalleyCoreBuilder withLocationExpander( final LocationExpander locations )
    {
        logger.debug( "Setting location expander: {}", locations );
        this.locationExpander = locations;
        return this;
    }

    public GalleyCoreBuilder withTransferDecorator( final TransferDecorator decorator )
    {
        this.decorator = decorator;
        return this;
    }

    public GalleyCoreBuilder withFileEvents( final FileEventManager events )
    {
        this.events = events;
        return this;
    }

    public GalleyCoreBuilder withCache( final CacheProvider cache )
    {
        this.cache = cache;
        return this;
    }

    public GalleyCoreBuilder withNfc( final NotFoundCache nfc )
    {
        this.nfc = nfc;
        return this;
    }

    public GalleyCoreBuilder withSpecialPathManager( final SpecialPathManager specialPathManager )
    {
        this.specialPathManager = specialPathManager;
        return this;
    }

    public TransportManager getTransportManager()
    {
        return transportManager;
    }

    public TransferManager getTransferManager()
    {
        return transferManager;
    }

    public GalleyCoreBuilder withTransportManager( final TransportManager transportManager )
    {
        this.transportManager = transportManager;
        return this;
    }

    public GalleyCoreBuilder withTransferManager( final TransferManager transferManager )
    {
        this.transferManager = transferManager;
        return this;
    }

    public List<Transport> getEnabledTransports()
    {
        return transports;
    }

    public GalleyCoreBuilder withEnabledTransports( final List<Transport> transports )
    {
        this.transports = transports;
        return this;
    }

    public GalleyCoreBuilder withEnabledTransports( final Transport... transports )
    {
        this.transports = new ArrayList<Transport>( Arrays.asList( transports ) );
        return this;
    }

    public ExecutorService getHandlerExecutor()
    {
        return handlerExecutor;
    }

    public GalleyCoreBuilder withHandlerExecutor( final ExecutorService handlerExecutor )
    {
        this.handlerExecutor = handlerExecutor;
        return this;
    }

    public ExecutorService getBatchExecutor()
    {
        return batchExecutor;
    }

    public GalleyCoreBuilder withBatchExecutor( final ExecutorService batchExecutor )
    {
        this.batchExecutor = batchExecutor;
        return this;
    }

    public File getCacheDir()
    {
        return cacheDir;
    }

    public GalleyCoreBuilder withCacheDir( final File cacheDir )
    {
        this.cacheDir = cacheDir;
        return this;
    }

    public LocationResolver getLocationResolver()
    {
        return locationResolver;
    }

    public GalleyCoreBuilder withLocationResolver( final LocationResolver locationResolver )
    {
        this.locationResolver = locationResolver;
        return this;
    }

    public GalleyCoreBuilder withAdditionalTransport( final Transport transport )
    {
        if ( transports == null )
        {
            transports = new ArrayList<Transport>();
        }

        transports.add( transport );

        return this;
    }
}
