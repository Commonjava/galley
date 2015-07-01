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

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;

public class GalleyCore
{

    private final LocationExpander locationExpander;

    private final LocationResolver locationResolver;

    private final TransferDecorator decorator;

    private final FileEventManager events;

    private final CacheProvider cache;

    private final NotFoundCache nfc;

    private final TransportManager transportManager;

    private final TransferManager transferManager;

    private final List<Transport> transports;

    private final ExecutorService handlerExecutor;

    private final ExecutorService batchExecutor;

    private final PasswordManager passwordManager;

    public GalleyCore( final LocationExpander locationExpander, final LocationResolver locationResolver,
                       final TransferDecorator decorator,
                           final FileEventManager events, final CacheProvider cache, final NotFoundCache nfc,
                           final TransportManager transportManager, final TransferManager transferManager,
                           final List<Transport> transports, final ExecutorService handlerExecutor,
 final ExecutorService batchExecutor,
                       final PasswordManager passwordManager )
    {
        this.locationExpander = locationExpander;
        this.locationResolver = locationResolver;
        this.decorator = decorator;
        this.events = events;
        this.cache = cache;
        this.nfc = nfc;
        this.transportManager = transportManager;
        this.transferManager = transferManager;
        this.transports = transports;
        this.handlerExecutor = handlerExecutor;
        this.batchExecutor = batchExecutor;
        this.passwordManager = passwordManager;
    }

    public PasswordManager getPasswordManager()
    {
        return passwordManager;
    }

    public LocationExpander getLocationExpander()
    {
        return locationExpander;
    }

    public LocationResolver getLocationResolver()
    {
        return locationResolver;
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

    public TransportManager getTransportManager()
    {
        return transportManager;
    }

    public TransferManager getTransferManager()
    {
        return transferManager;
    }

    public List<Transport> getEnabledTransports()
    {
        return transports;
    }

    public ExecutorService getHandlerExecutor()
    {
        return handlerExecutor;
    }

    public ExecutorService getBatchExecutor()
    {
        return batchExecutor;
    }
}
