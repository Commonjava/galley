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
package org.commonjava.maven.galley.cache.infinispan;

import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FastLocalCacheProviderBaseTest
{
    protected static EmbeddedCacheManager CACHE_MANAGER;

    protected final PathGenerator pathgen = new HashedLocationPathGenerator();

    protected final FileEventManager events = new TestFileEventManager();

    protected final TransferDecorator decorator = new TestTransferDecorator();

    protected final ExecutorService executor = Executors.newFixedThreadPool( 5 );

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    protected Cache<String, String> cache = CACHE_MANAGER.getCache( NFSOwnerCacheProducer.CACHE_NAME );


    @BeforeClass
    public static void setupClass()
    {
        CACHE_MANAGER = new NFSOwnerCacheProducer().getCacheMgr();
    }


    @Test( expected = java.lang.IllegalArgumentException.class )
    public void testConstructorWitNoNFSSysPath()
            throws IOException
    {
        Properties props = System.getProperties();
        props.remove( FastLocalCacheProvider.NFS_BASE_DIR_KEY );
        System.setProperties( props );
        final String NON_EXISTS_PATH = "";
        new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ),
                                    new SimpleCacheInstance<>( "test", cache ), pathgen, events, decorator, executor,
                                    NON_EXISTS_PATH );
    }

    @Test
    public void testConstructorWitNFSSysPath()
            throws IOException
    {
        System.setProperty( FastLocalCacheProvider.NFS_BASE_DIR_KEY, temp.newFolder().getCanonicalPath() );
        new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ),
                                    new SimpleCacheInstance<>( "test", cache ), pathgen, events, decorator, executor, null );
    }

}
