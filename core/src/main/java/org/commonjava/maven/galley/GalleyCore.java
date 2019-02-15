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
package org.commonjava.maven.galley;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.cdi.util.weft.WeftManaged;
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

    @Inject
    private LocationExpander locationExpander;

    @Inject
    private LocationResolver locationResolver;

    @Inject
    private TransferDecorator decorator;

    @Inject
    private FileEventManager events;

    @Inject
    private CacheProvider cache;

    @Inject
    private NotFoundCache nfc;

    @Inject
    private TransportManager transportManager;

    @Inject
    private TransferManager transferManager;

    @Inject
    private Instance<Transport> injectedTransports;

    private List<Transport> transports;

    @Inject
    @WeftManaged
    @ExecutorConfig( threads = 12, named = "galley-transfers", priority = 8,
                     loadSensitive = ExecutorConfig.BooleanLiteral.TRUE, maxLoadFactor = 100 )
    private ExecutorService handlerExecutor;

    @Inject
    @WeftManaged
    @ExecutorConfig( threads = 12, named = "galley-batching", priority = 8,
                     loadSensitive = ExecutorConfig.BooleanLiteral.TRUE, maxLoadFactor = 100 )
    private ExecutorService batchExecutor;

    @Inject
    private PasswordManager passwordManager;

    protected GalleyCore()
    {
    }

    public GalleyCore( final LocationExpander locationExpander, final LocationResolver locationResolver,
                       final TransferDecorator decorator, final FileEventManager events, final CacheProvider cache,
                       final NotFoundCache nfc, final TransportManager transportManager,
                       final TransferManager transferManager, final List<Transport> transports,
                       final ExecutorService handlerExecutor, final ExecutorService batchExecutor,
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

    @PostConstruct
    public void initInjections()
    {
        if ( transports == null && injectedTransports != null )
        {
            transports = new ArrayList<>();
            for ( final Transport transport : injectedTransports )
            {
                transports.add( transport );
            }
        }
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
