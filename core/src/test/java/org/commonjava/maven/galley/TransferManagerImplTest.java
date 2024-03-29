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
package org.commonjava.maven.galley;

import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.cache.MockPathGenerator;
import org.commonjava.maven.galley.cache.testutil.TestIOUtils;
import org.commonjava.maven.galley.config.TransportManagerConfig;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.internal.TransferManagerImpl;
import org.commonjava.maven.galley.internal.xfer.DownloadHandler;
import org.commonjava.maven.galley.internal.xfer.ExistenceHandler;
import org.commonjava.maven.galley.internal.xfer.ListingHandler;
import org.commonjava.maven.galley.internal.xfer.UploadHandler;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.io.SpecialPathManagerImpl;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.commonjava.maven.galley.transport.TransportManagerImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unit tests for the {@link TransferManagerImpl} itself. As far as possible, uses 
 * stubbed-out infrastructure components to isolate the behavior or this manager 
 * component ONLY, and avoid testing other component implementations in this 
 * class.
 * 
 * @author jdcasey
 */
public class TransferManagerImplTest
    extends AbstractTransferManagerTest
{

    private TransferManager mgr;

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private CacheProvider cacheProvider;

    private TestTransport transport;

    @Before
    public void setup()
    {
        transport = new TestTransport();
        TransportManager transportMgr = new TransportManagerImpl( transport );
        cacheProvider =
            new FileCacheProvider( TestIOUtils.newTempFolder( temp, "cache" ), new MockPathGenerator(), new NoOpFileEventManager(),
                                   new TransferDecoratorManager( new NoOpTransferDecorator() ), true );
        MemoryNotFoundCache nfc = new MemoryNotFoundCache();
        FileEventManager fileEvents = new NoOpFileEventManager();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        TransportManagerConfig transportManagerConfig = new TransportManagerConfig();

        final DownloadHandler dh = new DownloadHandler( nfc, transportManagerConfig, executor );
        final UploadHandler uh = new UploadHandler( nfc, transportManagerConfig, executor );
        final ListingHandler lh = new ListingHandler( nfc );
        final ExistenceHandler eh = new ExistenceHandler( nfc );

        mgr = new TransferManagerImpl( transportMgr, cacheProvider, nfc, fileEvents, dh, uh, lh, eh, new SpecialPathManagerImpl(), Executors.newFixedThreadPool( 2 ) );
    }

    @Override
    protected TransferManager getTransferManagerImpl()
    {
        return mgr;
    }

    @Override
    protected TestTransport getTransport()
    {
        return transport;
    }

    @Override
    protected CacheProvider getCacheProvider()
    {
        return cacheProvider;
    }

}
