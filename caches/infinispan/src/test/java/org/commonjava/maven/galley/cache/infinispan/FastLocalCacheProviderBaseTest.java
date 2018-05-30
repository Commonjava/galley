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

import org.apache.commons.io.IOUtils;
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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.commonjava.maven.galley.cache.infinispan.CacheTestUtil.LOCAL_CACHE_FILE_NAME_FOR_TEST;
import static org.commonjava.maven.galley.cache.infinispan.CacheTestUtil.getTestEmbeddedCacheManager;
import static org.junit.Assert.*;

public class FastLocalCacheProviderBaseTest
{
    protected static EmbeddedCacheManager CACHE_MANAGER;

    protected final PathGenerator pathgen = new HashedLocationPathGenerator();

    protected final FileEventManager events = new TestFileEventManager();

    protected final TransferDecorator decorator = new TestTransferDecorator();

    protected final ExecutorService executor = Executors.newFixedThreadPool( 5 );

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    protected static Cache<String, String> nfsOwnerCache;

    protected static Cache<String, ConcreteResource> localFileCache;

    @BeforeClass
    public static void setupClass()
    {
        CACHE_MANAGER = getTestEmbeddedCacheManager();
        nfsOwnerCache = CACHE_MANAGER.getCache( NFSOwnerCacheProducer.CACHE_NAME );
        localFileCache = CACHE_MANAGER.getCache( LOCAL_CACHE_FILE_NAME_FOR_TEST );
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
                                    new SimpleCacheInstance<>( "test", nfsOwnerCache ), pathgen, events, decorator, executor,
                                    NON_EXISTS_PATH, new SimpleCacheInstance<>( "localFileCache", localFileCache ));
    }

    @Test
    public void testConstructorWitNFSSysPath()
            throws IOException
    {
        System.setProperty( FastLocalCacheProvider.NFS_BASE_DIR_KEY, temp.newFolder().getCanonicalPath() );
        new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ),
                                    new SimpleCacheInstance<>( "test", nfsOwnerCache ), pathgen, events, decorator, executor,
                                    null, new SimpleCacheInstance<>( "localFileCache", localFileCache ) );
    }

    @Test
    public void testLocalFileCacheExpiration()
            throws IOException, InterruptedException
    {
        final File localDir = temp.newFolder();
        final File nfsDir = new File(localDir.getCanonicalPath()+"/nfs");
        System.setProperty( FastLocalCacheProvider.NFS_BASE_DIR_KEY, nfsDir.getCanonicalPath() );
        final PartyLineCacheProvider plcp = new PartyLineCacheProvider( localDir, pathgen, events, decorator );
        final FastLocalCacheProvider flcp =
                new FastLocalCacheProvider( plcp, new SimpleCacheInstance<>( "test", nfsOwnerCache ), pathgen, events,
                                            decorator, executor, null,
                                            new SimpleCacheInstance<>( "localFileCache", localFileCache ) );
        final Location loc = new SimpleLocation( "http://foo.com" );
        final ConcreteResource resource = new ConcreteResource( loc, String.format( "%s/%s", localDir, "name" ) );

        final String content = "This is test";

        // Test expiration for write
        try (OutputStream stream = flcp.openOutputStream( resource ))
        {
            IOUtils.write( content.getBytes(), stream );
        }

        assertTrue( plcp.exists( resource ) );
        assertTrue( flcp.getNFSDetachedFile( resource ).exists() );

        Thread.sleep( 5000L );

        assertFalse( plcp.exists( resource ) );
        assertTrue( flcp.getNFSDetachedFile( resource ).exists() );

        // Test expiration for read
        String read;
        try(InputStream in = flcp.openInputStream( resource )){
            read = IOUtils.toString( in );
        }

        assertEquals( content, read );
        assertTrue( plcp.exists( resource ) );
        assertTrue( flcp.getNFSDetachedFile( resource ).exists() );

        Thread.sleep( 5000L );

        assertFalse( plcp.exists( resource ) );
        assertTrue( flcp.getNFSDetachedFile( resource ).exists() );
    }



}
