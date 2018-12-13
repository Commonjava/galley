/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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

import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.nfc.NoOpNotFoundCache;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.testing.core.cache.TestCacheProvider;
import org.commonjava.maven.galley.testing.core.event.TestFileEventManager;
import org.commonjava.maven.galley.testing.core.io.TestTransferDecorator;
import org.commonjava.maven.galley.testing.core.transport.TestLocationExpander;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class ApiFixture
    extends ExternalResource
{

    private TemporaryFolder temp;

    private LocationExpander locations;

    private TransferDecorator decorator;

    private FileEventManager events;

    private TestCacheProvider cache;

    private TestTransport transport;

    private NotFoundCache nfc;

    private TransportManager transports;

    private TransferManager transfers;

    public ApiFixture()
    {
        temp = new TemporaryFolder();
    }

    public ApiFixture( final TemporaryFolder temp )
    {
        this.temp = temp;
    }

    public void initMissingComponents()
    {
        if ( locations == null )
        {
            locations = new TestLocationExpander();
        }

        if ( decorator == null )
        {
            decorator = new TestTransferDecorator();
        }

        if ( events == null )
        {
            events = new TestFileEventManager();
        }

        if ( cache == null )
        {
            cache = new TestCacheProvider( temp.newFolder( "cache" ), events, decorator );
        }

        if ( transport == null )
        {
            transport = new TestTransport();
        }

        if ( nfc == null )
        {
            nfc = new NoOpNotFoundCache();
        }
    }

    @Override
    public void before()
        throws Throwable
    {
        super.before();
        temp.create();
    }

    @Override
    public void after()
    {
        temp.delete();
        getTransport().clear();
        super.after();
    }

    public TemporaryFolder getTemp()
    {
        return temp;
    }

    public LocationExpander getLocations()
    {
        return locations;
    }

    public TransferDecorator getDecorator()
    {
        return decorator;
    }

    public FileEventManager getEvents()
    {
        return events;
    }

    public TestCacheProvider getCache()
    {
        return cache;
    }

    public TestTransport getTransport()
    {
        return transport;
    }

    public NotFoundCache getNfc()
    {
        return nfc;
    }

    public ApiFixture setTemp( final TemporaryFolder temp )
    {
        this.temp = temp;
        return this;
    }

    public ApiFixture setLocations( final LocationExpander locations )
    {
        this.locations = locations;
        return this;
    }

    public ApiFixture setDecorator( final TransferDecorator decorator )
    {
        this.decorator = decorator;
        return this;
    }

    public ApiFixture setEvents( final FileEventManager events )
    {
        this.events = events;
        return this;
    }

    public ApiFixture setCache( final TestCacheProvider cache )
    {
        this.cache = cache;
        return this;
    }

    public ApiFixture setTransport( final TestTransport transport )
    {
        this.transport = transport;
        return this;
    }

    public ApiFixture setNfc( final NotFoundCache nfc )
    {
        this.nfc = nfc;
        return this;
    }

    public TransportManager getTransports()
    {
        return transports;
    }

    public TransferManager getTransfers()
    {
        return transfers;
    }

    public ApiFixture setTransports( final TransportManager transports )
    {
        this.transports = transports;
        return this;
    }

    public ApiFixture setTransfers( final TransferManager transfers )
    {
        this.transfers = transfers;
        return this;
    }

}
