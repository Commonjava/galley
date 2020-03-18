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
package org.commonjava.maven.galley.cache.pathmapped;

import com.datastax.driver.core.Session;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.commonjava.maven.galley.cache.CacheProviderTCK;
import org.commonjava.maven.galley.cache.MockPathGenerator;
import org.commonjava.storage.pathmapped.config.DefaultPathMappedStorageConfig;
import org.commonjava.storage.pathmapped.pathdb.datastax.CassandraPathDB;
import org.commonjava.storage.pathmapped.core.FileBasedPhysicalStore;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.storage.pathmapped.model.Reclaim;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.commonjava.maven.galley.cache.testutil.AssertUtil.assertThrows;
import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.PROP_CASSANDRA_HOST;
import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.PROP_CASSANDRA_KEYSPACE;
import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.PROP_CASSANDRA_PORT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class PathMappedCacheProviderCassandraTest
                extends CacheProviderTCK
{

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private static DefaultPathMappedStorageConfig config;

    private static CassandraPathDB pathDB;

    private final FileEventManager events = new TestFileEventManager();
    private final TransferDecorator decorator = new TestTransferDecorator();

    private PathMappedFileManager fileManager;
    private PathMappedCacheProvider provider;

    @BeforeClass
    public static void startEmbeddedCassandra() throws Exception
    {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        Map<String, Object> props = new HashMap<>();
        props.put( PROP_CASSANDRA_HOST, "localhost" );
        props.put( PROP_CASSANDRA_PORT, 9142 );
        props.put( PROP_CASSANDRA_KEYSPACE, keyspace );

        config = new DefaultPathMappedStorageConfig( props );
        pathDB = new CassandraPathDB( config );

    }

    private static final String keyspace = "test";

    final PathGenerator pathgen = new MockPathGenerator();

    @Before
    public void setup() throws Exception
    {
        File baseDir = temp.newFolder();
        fileManager = new PathMappedFileManager( new DefaultPathMappedStorageConfig(), pathDB,
                                                 new FileBasedPhysicalStore( baseDir ) );
        provider = new PathMappedCacheProvider( baseDir, events, new TransferDecoratorManager( decorator ),
                                                Executors.newScheduledThreadPool( 2 ),
                                                fileManager, pathgen );
    }

    @After
    public void tearDown() throws Exception
    {
        Session session = pathDB.getSession();
        session.execute("TRUNCATE " + keyspace + ".pathmap");
        session.execute("TRUNCATE " + keyspace + ".reversemap");
        session.execute("TRUNCATE " + keyspace + ".reclaim");
        session.execute("TRUNCATE " + keyspace + ".filechecksum");
    }

    @Override
    protected CacheProvider getCacheProvider() throws Exception
    {
        return provider;
    }

    @Test
    public void moveAndReadNewFile() throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";

        final Location loc2 = new SimpleLocation( "http://bar.com" );

        final CacheProvider provider = getCacheProvider();
        final OutputStream out = provider.openOutputStream( new ConcreteResource( loc, fname ) );
        out.write( content.getBytes( UTF_8 ) );
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

        final String result = new String( baos.toByteArray(), UTF_8 );

        assertThat( result, equalTo( content ) );

        // source file should have been removed
        assertThrows( IOException.class, () -> provider.openInputStream( new ConcreteResource( loc, fname ) ) );
    }

    @Test
    public void deleteAndReclaim() throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";

        ConcreteResource resource = new ConcreteResource( loc, fname );

        final CacheProvider provider = getCacheProvider();

        final OutputStream out = provider.openOutputStream( resource );
        out.write( content.getBytes( UTF_8 ) );
        out.close();

        provider.delete( resource );

        assertThrows( IOException.class, () -> provider.openInputStream( resource ) );

        Transfer transfer = provider.getTransfer( resource );
        assertThat( transfer, notNullValue() );

        config.setGcGracePeriodInHours( 0 );
        List<Reclaim> l = pathDB.listOrphanedFiles();
        assertNotNull( l );
        assertFalse( l.isEmpty() );
        l.forEach( reclaim -> System.out.println( ">>> " + reclaim ) );
    }

    @Test
    public void replaceAndReclaim() throws Exception
    {
        final String content = "This is a test";
        final String content2 = "This is another test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";

        ConcreteResource resource = new ConcreteResource( loc, fname );

        final CacheProvider provider = getCacheProvider();

        // write once
        final OutputStream out = provider.openOutputStream( resource );
        out.write( content.getBytes( UTF_8 ) );
        out.close();

        // write again
        final OutputStream out2 = provider.openOutputStream( resource );
        out2.write( content2.getBytes( UTF_8 ) );
        out2.close();

        // read
        final InputStream in = provider.openInputStream( resource );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = -1;
        final byte[] buf = new byte[512];
        while ( ( read = in.read( buf ) ) > -1 )
        {
            baos.write( buf, 0, read );
        }
        final String result = new String( baos.toByteArray(), UTF_8 );
        assertThat( result, equalTo( content2 ) );

        // check reclaim
        config.setGcGracePeriodInHours( 0 );
        List<Reclaim> l = pathDB.listOrphanedFiles();
        assertNotNull( l );
        assertFalse( l.isEmpty() );
        l.forEach( reclaim -> System.out.println( ">>> " + reclaim ) );
    }

}
