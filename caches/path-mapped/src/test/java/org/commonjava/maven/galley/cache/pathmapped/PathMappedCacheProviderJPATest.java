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
package org.commonjava.maven.galley.cache.pathmapped;

import org.commonjava.maven.galley.cache.CacheProviderTCK;
import org.commonjava.maven.galley.cache.MockPathGenerator;
import org.commonjava.maven.galley.io.SpecialPathManagerImpl;
import org.commonjava.storage.pathmapped.config.DefaultPathMappedStorageConfig;
import org.commonjava.storage.pathmapped.core.FileBasedPhysicalStore;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.storage.pathmapped.pathdb.jpa.JPAPathDB;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import static org.commonjava.maven.galley.cache.testutil.AssertUtil.assertThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PathMappedCacheProviderJPATest
        extends CacheProviderTCK
{

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private PathMappedCacheProvider provider;

    @Before
    public void setup()
            throws Exception
    {
        final FileEventManager events = new TestFileEventManager();
        final TransferDecorator decorator = new TestTransferDecorator();
        final PathGenerator pathgen = new MockPathGenerator();

        File baseDir = temp.newFolder();
        provider = new PathMappedCacheProvider( baseDir, events, new TransferDecoratorManager( decorator ), null,
                                                Executors.newScheduledThreadPool( 2 ),
                                                new PathMappedFileManager( new DefaultPathMappedStorageConfig(),
                                                                           new JPAPathDB( "test" ),
                                                                           new FileBasedPhysicalStore( baseDir ) ),
                                                pathgen, new SpecialPathManagerImpl() );
    }

    @Override
    protected CacheProvider getCacheProvider()
    {
        return provider;
    }

    @Test
    public void moveAndReadNewFile()
            throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";

        final Location loc2 = new SimpleLocation( "http://bar.com" );

        final CacheProvider provider = getCacheProvider();
        final OutputStream out = provider.openOutputStream( new ConcreteResource( loc, fname ) );
        out.write( content.getBytes( StandardCharsets.UTF_8 ) );
        out.close();

        provider.move( new ConcreteResource( loc, fname ), new ConcreteResource( loc2, fname ) );

        final InputStream in = provider.openInputStream( new ConcreteResource( loc2, fname ) );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = -1;
        final byte[] buf = new byte[512];
        while ( ( read = in.read( buf ) ) > -1 )
        {
            baos.write( buf, 0, read );
        }

        final String result = new String( baos.toByteArray(), StandardCharsets.UTF_8 );

        assertThat( result, equalTo( content ) );

        // source file should have been removed
        assertThrows( IOException.class, () -> {
            //noinspection EmptyTryBlock
            try (InputStream ignored = provider.openInputStream( new ConcreteResource( loc, fname ) ))
            {

            }
        } );
    }

}
