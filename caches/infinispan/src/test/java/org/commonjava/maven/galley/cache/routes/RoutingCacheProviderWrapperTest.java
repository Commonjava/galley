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
package org.commonjava.maven.galley.cache.routes;

import org.commonjava.maven.galley.cache.CacheProviderFactory;
import org.commonjava.maven.galley.cache.infinispan.FastLocalCacheProvider;
import org.commonjava.maven.galley.cache.infinispan.FastLocalCacheProviderFactory;
import org.commonjava.maven.galley.cache.infinispan.NFSOwnerCacheProducer;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProviderFactory;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.SpecialPathMatcher;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RoutingCacheProviderWrapperTest
{
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private CacheProviderFactory partylineFac;

    private CacheProviderFactory fastLocalFac;

    private final PathGenerator pathgen = new HashedLocationPathGenerator();

    private final FileEventManager events = new TestFileEventManager();

    private final TransferDecorator decorator = new TestTransferDecorator();

    private final RouteSelector selector = new RouteSelector()
    {
        @Override
        public boolean isDisposable( ConcreteResource resource )
        {
            if ( resource != null )
            {
                String pattern = "^hosted:.*$";
                final Location loc = resource.getLocation();
                if ( loc != null )
                {
                    final String uri = loc.getUri();
                    synchronized ( this )
                    {
                        return uri != null && uri.matches( pattern );
                    }
                }
            }
            return false;
        }
    };

    @Before
    public void prepare()
            throws Exception
    {

        final File cacheDir = temp.newFolder();
        partylineFac = new PartyLineCacheProviderFactory( cacheDir );
        fastLocalFac = new FastLocalCacheProviderFactory( cacheDir, temp.newFolder(), new DefaultCacheManager().<String, String>getCache(),
                                                          Executors.newFixedThreadPool( 5 ) );
    }

    @Test
    public void test()
            throws Exception
    {

        final RoutingCacheProviderWrapper router =
                (RoutingCacheProviderWrapper) new RoutingCacheProviderFactory( selector, fastLocalFac,
                                                                               partylineFac ).create( pathgen,
                                                                                                      decorator,
                                                                                                      events );
        final CacheProvider partyline = partylineFac.create( pathgen, decorator, events );
        final CacheProvider fastLocal = fastLocalFac.create( pathgen, decorator, events );

        final String fname = "/path/to/my/file.txt";
        Location loc = new SimpleLocation( "remote:foo/com" );
        ConcreteResource resource = new ConcreteResource( loc, fname );
        CacheProvider get = router.getRoutedProvider( resource );
        assertThat( get, equalTo( partyline ) );

        loc = new SimpleLocation( "hosted:foo/com" );
        resource = new ConcreteResource( loc, fname );
        get = router.getRoutedProvider( resource );
        assertThat( get, equalTo( fastLocal ) );

        loc = new SimpleLocation( "group:foo/com" );
        resource = new ConcreteResource( loc, fname );
        get = router.getRoutedProvider( resource );
        assertThat( get, equalTo( partyline ) );

        loc = new SimpleLocation( "http://foo.com" );
        resource = new ConcreteResource( loc, fname );
        get = router.getRoutedProvider( resource );
        assertThat( get, equalTo( partyline ) );

        resource = new ConcreteResource( null, null );
        get = router.getRoutedProvider( resource );
        assertThat( get, equalTo( partyline ) );

        get = router.getRoutedProvider( null );
        assertThat( get, equalTo( partyline ) );
    }

}
