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
package org.commonjava.maven.galley.cache.routes;

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.cache.CacheProviderFactory;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

public class RoutingCacheProviderFactory
        implements CacheProviderFactory
{
    private RouteSelector selector;

    private CacheProvider disposable;

    private CacheProvider safe;

    private CacheProviderFactory disposableFactory;

    private CacheProviderFactory safeFactory;

    private transient RoutingCacheProviderWrapper provider;

    public RoutingCacheProviderFactory( final RouteSelector selector, final CacheProvider disposable,
                                        final CacheProvider safe )
    {
        this.selector = selector;
        this.disposable = disposable;
        this.safe = safe;
    }

    public RoutingCacheProviderFactory( final RouteSelector selector, final CacheProviderFactory disposableFactory,
                                        final CacheProviderFactory safeFactory )
    {
        this.selector = selector;
        this.disposableFactory = disposableFactory;
        this.safeFactory = safeFactory;
    }

    @Override
    public synchronized CacheProvider create( PathGenerator pathGenerator, TransferDecorator transferDecorator,
                                              FileEventManager fileEventManager )
            throws GalleyInitException
    {
        if ( provider == null )
        {
            if ( disposableFactory != null )
            {
                disposable = disposableFactory.create( pathGenerator, transferDecorator, fileEventManager );
            }
            if ( safeFactory != null )
            {
                safe = safeFactory.create( pathGenerator, transferDecorator, fileEventManager );
            }
            provider = new RoutingCacheProviderWrapper( selector, disposable, safe );
        }

        return provider;
    }
}
