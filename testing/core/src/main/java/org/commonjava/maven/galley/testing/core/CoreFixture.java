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
package org.commonjava.maven.galley.testing.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.commonjava.maven.galley.GalleyCore;
import org.commonjava.maven.galley.GalleyCoreBuilder;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.cache.FileCacheProviderFactory;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.testing.core.cache.TestCacheProvider;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreFixture
    extends ExternalResource
{

    private GalleyCore core;

    private TemporaryFolder temp;

    private GalleyCoreBuilder coreBuilder;

    private TestTransport testTransport;

    private final boolean autoInit;

    private File cacheDir;

    public CoreFixture()
    {
        this.autoInit = true;
        this.temp = new TemporaryFolder();
    }

    public CoreFixture( final TemporaryFolder temp )
    {
        this.autoInit = true;
        this.temp = temp;
    }

    public CoreFixture( final boolean autoInit )
    {
        this.autoInit = autoInit;
        this.temp = new TemporaryFolder();
    }

    public CoreFixture( final boolean autoInit, final TemporaryFolder temp )
    {
        this.autoInit = autoInit;
        this.temp = temp;
    }

    public void initGalley()
        throws IOException
    {
        temp.create();
        coreBuilder = new GalleyCoreBuilder( new FileCacheProviderFactory( temp.newFolder( "cache" ) ) );
    }

    public void initTestTransport()
    {
        System.out.println( "Initializing test transport directly" );
        testTransport = new TestTransport();
        coreBuilder.withAdditionalTransport( testTransport );
    }

    public void initMissingComponents()
        throws Exception
    {
        if ( coreBuilder == null )
        {
            initGalley();
        }

        final List<Transport> transports = coreBuilder.getEnabledTransports();
        if ( transports == null || transports.isEmpty() )
        {
            System.out.println( "Initializing test transport implicitly" );
            initTestTransport();
        }

        coreBuilder.initMissingComponents();
        if ( temp != null && cacheDir == null )
        {
            cacheDir = temp.newFolder( "cache" );
        }

        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug(
                "Initializing FileCacheProvider with:\n  Cache directory: {}\n  PathGenerator: {}\n  FileEventManager: {}\n  TransferDecorator: {}",
                cacheDir, coreBuilder.getPathGenerator(), coreBuilder.getFileEvents(),
                coreBuilder.getTransferDecorator() );

        coreBuilder.withCache( new FileCacheProvider( cacheDir, coreBuilder.getPathGenerator(),
                                                      coreBuilder.getFileEvents(),
                                                      coreBuilder.getTransferDecorator() ) );
    }

    @Override
    protected void before()
        throws Throwable
    {
        if ( autoInit )
        {
            initMissingComponents();
            core = coreBuilder.build();
        }
        super.before();
    }

    @Override
    protected void after()
    {
        core = null;
        super.after();
    }

    public TemporaryFolder getTemp()
    {
        return temp;
    }

    public TestTransport getTransport()
    {
        return testTransport;
    }

    public TestTransport getTestTransport()
    {
        return testTransport;
    }

    public PasswordManager getPasswordManager()
    {
        return core == null ? coreBuilder.getPasswordManager() : core.getPasswordManager();
    }

    public LocationExpander getLocationExpander()
    {
        return core == null ? coreBuilder.getLocationExpander() : core.getLocationExpander();
    }

    public LocationResolver getLocationResolver()
    {
        return core == null ? coreBuilder.getLocationResolver() : core.getLocationResolver();
    }

    public TransferDecorator getTransferDecorator()
    {
        return core == null ? coreBuilder.getTransferDecorator() : core.getTransferDecorator();
    }

    public FileEventManager getFileEvents()
    {
        return core == null ? coreBuilder.getFileEvents() : core.getFileEvents();
    }

    public CacheProvider getCache()
    {
        return core == null ? coreBuilder.getCache() : core.getCache();
    }

    public NotFoundCache getNfc()
    {
        return core == null ? coreBuilder.getNfc() : core.getNfc();
    }

    public TransportManager getTransportManager()
    {
        return core == null ? coreBuilder.getTransportManager() : core.getTransportManager();
    }

    public TransferManager getTransferManager()
    {
        return core == null ? coreBuilder.getTransferManager() : core.getTransferManager();
    }

    public List<Transport> getEnabledTransports()
    {
        return core == null ? coreBuilder.getEnabledTransports() : core.getEnabledTransports();
    }

    public ExecutorService getHandlerExecutor()
    {
        return core == null ? coreBuilder.getHandlerExecutor() : core.getHandlerExecutor();
    }

    public ExecutorService getBatchExecutor()
    {
        return core == null ? coreBuilder.getBatchExecutor() : core.getBatchExecutor();
    }

    public CoreFixture withPasswordManager( final PasswordManager passwordManager )
    {
        checkInitialized();
        coreBuilder.withPasswordManager( passwordManager );
        return this;
    }

    public CoreFixture withLocationExpander( final LocationExpander locations )
    {
        checkInitialized();
        coreBuilder.withLocationExpander( locations );
        return this;
    }

    public CoreFixture withTransferDecorator( final TransferDecorator decorator )
    {
        checkInitialized();
        coreBuilder.withTransferDecorator( decorator );
        return this;
    }

    public CoreFixture withFileEvents( final FileEventManager events )
    {
        checkInitialized();
        coreBuilder.withFileEvents( events );
        return this;
    }

    public CoreFixture withCache( final CacheProvider cache )
    {
        checkInitialized();
        coreBuilder.withCache( cache );
        return this;
    }

    public CoreFixture withNfc( final NotFoundCache nfc )
    {
        checkInitialized();
        coreBuilder.withNfc( nfc );
        return this;
    }

    public CoreFixture withTransportManager( final TransportManager transportManager )
    {
        checkInitialized();
        coreBuilder.withTransportManager( transportManager );
        return this;
    }

    public CoreFixture withTransferManager( final TransferManager transferManager )
    {
        checkInitialized();
        coreBuilder.withTransferManager( transferManager );
        return this;
    }

    public CoreFixture withEnabledTransports( final List<Transport> transports )
    {
        checkInitialized();
        coreBuilder.withEnabledTransports( transports );
        return this;
    }

    public CoreFixture withEnabledTransports( final Transport... transports )
    {
        checkInitialized();
        coreBuilder.withEnabledTransports( transports );
        return this;
    }

    public CoreFixture withHandlerExecutor( final ExecutorService handlerExecutor )
    {
        checkInitialized();
        coreBuilder.withHandlerExecutor( handlerExecutor );
        return this;
    }

    public CoreFixture withBatchExecutor( final ExecutorService batchExecutor )
    {
        checkInitialized();
        coreBuilder.withBatchExecutor( batchExecutor );
        return this;
    }

    public File getCacheDir()
    {
        return cacheDir;
    }

    public CoreFixture withCacheDir( final File cacheDir )
    {
        checkInitialized();
        this.cacheDir = cacheDir;
        return this;
    }

    public CoreFixture withLocationResolver( final LocationResolver locationResolver )
    {
        checkInitialized();
        coreBuilder.withLocationResolver( locationResolver );
        return this;
    }

    public CoreFixture withAdditionalTransport( final Transport transport )
    {
        checkInitialized();
        coreBuilder.withAdditionalTransport( transport );
        return this;
    }

    public CoreFixture setTemp( final TemporaryFolder temp )
        throws IOException
    {
        if ( this.temp != null )
        {
            this.temp.delete();
        }

        this.temp = temp;
        this.temp.create();
        return this;
    }

    private void checkInitialized()
    {
        if ( core != null )
        {
            throw new IllegalStateException( "Already initialized!" );
        }
    }

    @Deprecated
    public LocationExpander getLocations()
    {
        return core == null ? coreBuilder.getLocationExpander() : core.getLocationExpander();
    }

    @Deprecated
    public TransferDecorator getDecorator()
    {
        return core == null ? coreBuilder.getTransferDecorator() : core.getTransferDecorator();
    }

    @Deprecated
    public FileEventManager getEvents()
    {
        return core == null ? coreBuilder.getFileEvents() : core.getFileEvents();
    }

    @Deprecated
    public CoreFixture setLocations( final LocationExpander locations )
    {
        checkInitialized();
        coreBuilder.withLocationExpander( locations );
        return this;
    }

    @Deprecated
    public CoreFixture setDecorator( final TransferDecorator decorator )
    {
        checkInitialized();
        coreBuilder.withTransferDecorator( decorator );
        return this;
    }

    @Deprecated
    public CoreFixture setEvents( final FileEventManager events )
    {
        checkInitialized();
        coreBuilder.withFileEvents( events );
        return this;
    }

    @Deprecated
    public CoreFixture setCache( final TestCacheProvider cache )
    {
        checkInitialized();
        coreBuilder.withCache( cache );
        return this;
    }

    @Deprecated
    public CoreFixture setTransport( final TestTransport transport )
    {
        checkInitialized();
        this.testTransport = transport;
        return this;
    }

    @Deprecated
    public CoreFixture setNfc( final NotFoundCache nfc )
    {
        checkInitialized();
        coreBuilder.withNfc( nfc );
        return this;
    }

    @Deprecated
    public TransportManager getTransports()
    {
        return core == null ? coreBuilder.getTransportManager() : core.getTransportManager();
    }

    @Deprecated
    public TransferManager getTransfers()
    {
        return core == null ? coreBuilder.getTransferManager() : core.getTransferManager();
    }

    @Deprecated
    public CoreFixture setTransports( final TransportManager transports )
    {
        checkInitialized();
        coreBuilder.withTransportManager( transports );
        return this;
    }

    @Deprecated
    public CoreFixture setTransfers( final TransferManager transfers )
    {
        checkInitialized();
        coreBuilder.withTransferManager( transfers );
        return this;
    }

}
