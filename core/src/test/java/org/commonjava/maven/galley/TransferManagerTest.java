/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.internal.xfer.DownloadHandler;
import org.commonjava.maven.galley.internal.xfer.ExistenceHandler;
import org.commonjava.maven.galley.internal.xfer.ListingHandler;
import org.commonjava.maven.galley.internal.xfer.UploadHandler;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.commonjava.maven.galley.transport.TransportManagerImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for the {@link TransferManagerImpl} itself. As far as possible, uses 
 * stubbed-out infrastructure components to isolate the behavior or this manager 
 * component ONLY, and avoid testing other component implementations in this 
 * class.
 * 
 * @author jdcasey
 */
public class TransferManagerTest
    extends AbstractTransferManagerTest
{

    private TransferManagerImpl mgr;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private TransportManager transportMgr;

    private CacheProvider cacheProvider;

    private FileEventManager fileEvents;

    private ExecutorService executor;

    private TestTransport transport;

    private MemoryNotFoundCache nfc;

    @Before
    public void setup()
    {
        transport = new TestTransport();
        transportMgr = new TransportManagerImpl( transport );
        cacheProvider =
            new FileCacheProvider( temp.newFolder( "cache" ), new HashedLocationPathGenerator(), new NoOpFileEventManager(),
                                   new NoOpTransferDecorator(), true );
        nfc = new MemoryNotFoundCache();
        fileEvents = new NoOpFileEventManager();
        executor = Executors.newSingleThreadExecutor();

        final DownloadHandler dh = new DownloadHandler( nfc, executor );
        final UploadHandler uh = new UploadHandler( nfc, executor );
        final ListingHandler lh = new ListingHandler( nfc );
        final ExistenceHandler eh = new ExistenceHandler( nfc );

        mgr = new TransferManagerImpl( transportMgr, cacheProvider, nfc, fileEvents, dh, uh, lh, eh, Executors.newFixedThreadPool( 2 ) );
    }

    @Override
    protected TransferManagerImpl getTransferManagerImpl()
        throws Exception
    {
        return mgr;
    }

    @Override
    protected TestTransport getTransport()
        throws Exception
    {
        return transport;
    }

    @Override
    protected CacheProvider getCacheProvider()
        throws Exception
    {
        return cacheProvider;
    }

}
