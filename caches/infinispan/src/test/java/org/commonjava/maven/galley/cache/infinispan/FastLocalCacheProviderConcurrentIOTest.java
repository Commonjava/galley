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
package org.commonjava.maven.galley.cache.infinispan;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.cache.iotasks.DeleteTask;
import org.commonjava.maven.galley.cache.iotasks.ReadTask;
import org.commonjava.maven.galley.cache.iotasks.WriteTask;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestIOUtils;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.util.partyline.Partyline;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.commonjava.maven.galley.cache.infinispan.CacheTestUtil.getTestEmbeddedCacheManager;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith( org.jboss.byteman.contrib.bmunit.BMUnitRunner.class )
@BMUnitConfig( loadDirectory = "target/test-classes/bmunit", debug = true )
@Deprecated
@Ignore
public class FastLocalCacheProviderConcurrentIOTest
{
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private static EmbeddedCacheManager CACHE_MANAGER;

    private final String content = "Testing";

    private final String diffContent = "This is different from content";

    private CountDownLatch latch;

    private FastLocalCacheProvider provider;

    private final PathGenerator pathgen = new HashedLocationPathGenerator();

    private final FileEventManager events = new TestFileEventManager();

    private final TransferDecorator decorator = new TestTransferDecorator();

    private final Cache<String, String> cache = CACHE_MANAGER.getCache( NFSOwnerCacheProducer.CACHE_NAME );

    private final Cache<String, ConcreteResource> localFileCache = CACHE_MANAGER.getCache( "simpleLocalFileCacheTest" );

    private PartyLineCacheProvider plProvider;

    private final ExecutorService executor = Executors.newFixedThreadPool( 4 );

    private final ExecutorService testPool = Executors.newFixedThreadPool( 2 );

    private final long WAIT_TIMEOUT_SECONDS = 300;

    @BeforeClass
    public static void setupClass()
    {
        CACHE_MANAGER = getTestEmbeddedCacheManager();
    }

    @Before
    public void setup()
            throws Exception
    {
        final String nfsBasePath = createNFSBaseDir( temp.newFolder().getCanonicalPath() );
        plProvider = new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator,
                                                 Executors.newScheduledThreadPool( 2 ), new Partyline() );

        provider = new FastLocalCacheProvider( plProvider, new SimpleCacheInstance<>( "test", cache ), pathgen, events,
                                               decorator, executor, nfsBasePath, new SimpleCacheInstance<>( "localFileCache", localFileCache ));

        latch = new CountDownLatch( 2 );
    }

    @Test
    @BMScript( "TryToReadWhileWriting.btm" )
    public void testReadWrite()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_read_write.txt" );
        testPool.execute( new WriteTask( provider, content, resource, latch, 1000 ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        if ( !TestIOUtils.latchWait( latch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS ) )
        {
            fail( "I/O timeout" );
        }

        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( content ) );
        assertThat( provider.exists( resource ), equalTo( true ) );
        assertThat( readLocalResource( resource ), equalTo( content ) );
        assertThat( readNFSResource( resource ), equalTo( content ) );
    }

    @Test
    @BMScript( "TryToReadWhileWriting.btm" )
    public void testBigFileReadWrite()
            throws Exception
    {
        StringBuilder builder = new StringBuilder();
        int loop = 1024 * 1024 * 10;
        for ( int i = 0; i < loop; i++ )
        {
            builder.append( content );
        }
        final String bigContent = builder.toString();
        System.out.println( String.format( "the content size is: %dM", ( bigContent.length() / 1024 / 1024 ) ) );
        final ConcreteResource resource = createTestResource( "file_read_write.txt" );
        testPool.execute( new WriteTask( provider, bigContent, resource, latch ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        if ( !TestIOUtils.latchWait( latch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS ) )
        {
            fail( "I/O timeout" );
        }

        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( bigContent ) );
        assertThat( provider.exists( resource ), equalTo( true ) );
        assertThat( readLocalResource( resource ), equalTo( bigContent ) );
        assertThat( readNFSResource( resource ), equalTo( bigContent ) );
    }

    @Test
    @BMScript( "TryToWriteWhileReading.btm" )
    public void tesWriteReadWithLocal()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_write_read.txt" );
        prepareBothResource( resource, content );
        testPool.execute( new WriteTask( provider, diffContent, resource, latch, 1000 ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        if ( !TestIOUtils.latchWait( latch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS ) )
        {
            fail( "I/O timeout" );
        }

        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( content ) );
        final String changedResult = readLocalResource( resource );
        assertThat( changedResult, equalTo( diffContent ) );
    }

    @Test
    @BMScript( "TryToWriteWhileReading.btm" )
    public void testWriteReadWithNFS()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_write_read_has_only_NFS.txt" );
        prepareNFSResource( resource, content );
        testPool.execute( new WriteTask( provider, diffContent, resource, latch ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        if ( !TestIOUtils.latchWait( latch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS ) )
        {
            fail( "I/O timeout" );
        }

        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( content ) );
        final String changedResult = readLocalResource( resource );
        assertThat( changedResult, equalTo( diffContent ) );
    }

    @Test
    @BMScript( "TryToWriteThenWaitReadingStreamOpen.btm" )
    public void testWriteWhenReadOpenWithNFS()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_write_wait_read_has_only_NFS.txt" );
        prepareNFSResource( resource, content );
        testPool.execute( new WriteTask( provider, diffContent, resource, latch ) );
        //make sure write task execute first
        Thread.sleep( 500 );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch, 500 ) );

        if ( !TestIOUtils.latchWait( latch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS ) )
        {
            fail( "I/O timeout" );
        }

        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( diffContent ) );
        final String changedResult = readLocalResource( resource );
        assertThat( changedResult, equalTo( diffContent ) );
    }

    @Test
    @BMScript( "TryToWriteWhileReading.btm" )
    public void testWriteReadWithNoResource()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_write_read_no_both_resource.txt" );
        testPool.execute( new WriteTask( provider, content, resource, latch ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        if ( !TestIOUtils.latchWait( latch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS ) )
        {
            fail( "I/O timeout" );
        }

        final String readingResult = readingFuture.get();
        assertNull( readingResult );
        final String changedResult = readLocalResource( resource );
        assertThat( changedResult, equalTo( content ) );
    }

    @Test
    public void testBothWrite()
            throws Exception
    {
        Map<String, String> fileFolderMap = new HashMap<>( 1 );
        fileFolderMap.put( "file_both_write.txt", "/path/to/my" );
        multiWriteOnFilesInFolder( fileFolderMap, 2 );
    }

    @Test
    @BMScript( "common/SimultaneousWritesResourceExistence.btm" )
    public void testBothWriteOnDiffFilesInSameFolder()
            throws Exception
    {
        Map<String, String> fileFolderMap = new HashMap<>( 2 );
        fileFolderMap.put( "file1.txt", "/path/to/my" );
        fileFolderMap.put( "file2.txt", "/path/to/my" );
        multiWriteOnFilesInFolder( fileFolderMap, 1 );
    }

    @Test
    @BMScript( "common/SimultaneousWritesResourceExistence.btm" )
    public void testBothWriteOnDiffFilesInDiffFolder()
            throws Exception
    {
        Map<String, String> fileFolderMap = new HashMap<>( 2 );
        fileFolderMap.put( "file1.txt", "/path/to/my/folder1" );
        fileFolderMap.put( "file2.txt", "/path/to/my/folder2" );
        multiWriteOnFilesInFolder( fileFolderMap, 1 );
    }

    private void multiWriteOnFilesInFolder( final Map<String, String> fileFolderMap, final int threads )
            throws Exception
    {
        List<ConcreteResource> resources = new ArrayList<>( fileFolderMap.size() );
        for ( String fname : fileFolderMap.keySet() )
        {
            final String folder = fileFolderMap.get( fname );
            resources.add( createTestResource( fname, folder ) );
        }

        final CountDownLatch waitLatch = new CountDownLatch( resources.size() * threads );

        for ( ConcreteResource resource : resources )
        {
            IntStream.range( 0, threads )
                     .forEach( i -> testPool.execute( new WriteTask( provider, content, resource, waitLatch ) ) );
        }

        TestIOUtils.latchWait( waitLatch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS );

        for ( ConcreteResource resource : resources )
        {
            final String localResult = readLocalResource( resource );
            final String nfsResult = readNFSResource( resource );
            assertThat( localResult, equalTo( nfsResult ) );
        }
    }

    @Test
    public void testBothReadWithNFS()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_both_read_has_only_NFS.txt" );
        prepareNFSResource( resource, content );
        final Future<String> readingFuture1 =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );
        final Future<String> readingFuture2 =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        assertLatchWait();

        final String readingResult1 = readingFuture1.get();
        assertThat( readingResult1, equalTo( content ) );
        final String readingResult2 = readingFuture2.get();
        assertThat( readingResult2, equalTo( content ) );
    }

    @Test
    @BMScript( "TryToDeleteWhileReadingCompleted.btm" )
    public void testDeleteWhenReadCompleted()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_delete_read.txt" );
        prepareBothResource( resource, content );
        final Future<Boolean> deleteFuture =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        if ( !TestIOUtils.latchWait( latch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS ) )
        {
            fail( "I/O timeout" );
        }

        final Boolean deleted = deleteFuture.get();
        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( content ) );
        assertThat( deleted, equalTo( true ) );
        assertThat( provider.exists( resource ), equalTo( false ) );
    }

    @Test
    @BMScript( "TryToDeleteWhileReadingNotCompleted.btm" )
    public void testDeleteWhenReadNotCompleted()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_delete_read_not_completed.txt" );
        prepareBothResource( resource, content );
        final Future<Boolean> deleteFuture =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch, 1000 ) );

        assertLatchWait();

        final Boolean deleted = deleteFuture.get();
        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( content ) );
        assertThat( deleted, equalTo( false ) );
        assertThat( provider.exists( resource ), equalTo( true ) );
    }

    @Test
    @BMScript( "TryToDeleteWhileReadingNotCompleted.btm" )
    public void testDeleteWhenReadNotCompletedWithNFS()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_delete_read_not_completed_nfs_only.txt" );
        prepareNFSResource( resource, content );
        final Future<Boolean> deleteFuture =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch, 1000 ) );

        assertLatchWait();

        final Boolean deleted = deleteFuture.get();
        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( content ) );
        assertThat( deleted, equalTo( false ) );
        assertThat( provider.exists( resource ), equalTo( true ) );
    }

    @Test
    @BMScript( "TryToReadWhileDeleteCompleted.btm" )
    public void testReadWhileDeleteCompleted()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_read_delete_completed.txt" );
        prepareBothResource( resource, content );
        final Future<Boolean> deleteFuture =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        assertLatchWait();

        final Boolean deleted = deleteFuture.get();
        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( null ) );
        assertThat( deleted, equalTo( true ) );
        assertThat( provider.exists( resource ), equalTo( false ) );
    }

    @Test
    @BMScript( "TryToDeleteWhileWritingCompleted.btm" )
    public void testDeleteWhenWriteCompleted()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_delete_write_completed.txt" );
        final Future<Boolean> deleteFuture =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );
        testPool.execute( new WriteTask( provider, content, resource, latch ) );

        assertLatchWait();

        final Boolean deleted = deleteFuture.get();
        assertThat( deleted, equalTo( true ) );
        assertThat( provider.exists( resource ), equalTo( false ) );
    }

    @Test
    @BMScript( "TryToDeleteWhileWritingNotCompleted.btm" )
    public void testDeleteWhenWriteNotCompleted()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_delete_write_not_completed.txt" );
        final Future<Boolean> deleteFuture =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );
        testPool.execute( new WriteTask( provider, content, resource, latch, 1000 ) );

        assertLatchWait();

        final Boolean deleted = deleteFuture.get();
        assertThat( deleted, equalTo( false ) );
        assertThat( provider.exists( resource ), equalTo( true ) );
    }

    @Test
    @BMScript( "TryToWriteWhileDeleteCompleted.btm" )
    public void testWriteWhenDeleteCompleted()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_write_delete_completed.txt" );
        prepareBothResource( resource, content );
        final Future<Boolean> deleteFuture =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );
        testPool.execute( new WriteTask( provider, content, resource, latch ) );

        assertLatchWait();

        final Boolean deleted = deleteFuture.get();
        assertThat( deleted, equalTo( true ) );
        assertThat( provider.exists( resource ), equalTo( true ) );
    }

    @Test
    public void testBothDelete()
            throws Exception
    {
        final ConcreteResource resource = createTestResource( "file_both_delete.txt" );
        prepareBothResource( resource, content );
        final Future<Boolean> deleteFurture1 =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );
        final Future<Boolean> deleteFurture2 =
                testPool.submit( (Callable<Boolean>) new DeleteTask( provider, content, resource, latch ) );

        assertLatchWait();

        final Boolean deleted1 = deleteFurture1.get();
        final Boolean deleted2 = deleteFurture2.get();
        assertThat( deleted1 && deleted2, equalTo( false ) );
        assertThat( deleted1 || deleted2, equalTo( true ) );
        assertThat( provider.exists( resource ), equalTo( false ) );
    }

    private String createNFSBaseDir( String tempBaseDir )
            throws IOException
    {
        final File file = new File( tempBaseDir + "/mnt/nfs" );
        file.delete();
        file.mkdirs();
        return file.getCanonicalPath();
    }

    private void prepareBothResource( ConcreteResource resource, String content )
            throws IOException
    {
        prepareLocalResource( resource, content );
        prepareNFSResource( resource, content );
    }

    private void prepareLocalResource( ConcreteResource resource, String content )
            throws IOException
    {
        OutputStream stream = plProvider.openOutputStream( resource );
        IOUtils.write( content.getBytes(), stream );
        stream.close();
    }

    private void prepareNFSResource( ConcreteResource resource, String content )
            throws IOException
    {
        final File file = provider.getNFSDetachedFile( resource );
        if ( !file.exists() )
        {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        OutputStream stream = new FileOutputStream( file );
        IOUtils.write( content.getBytes(), stream );
        stream.close();
    }

    private String readLocalResource( ConcreteResource resource )
            throws IOException
    {
        return TestIOUtils.readFromStream( plProvider.openInputStream( resource ) );
    }

    private String readNFSResource( ConcreteResource resource )
            throws IOException
    {
        return TestIOUtils.readFromStream( new FileInputStream( provider.getNFSDetachedFile( resource ) ) );
    }

    private ConcreteResource createTestResource( String fname )
    {
        return createTestResource( fname, "/path/to/my" );
    }

    private ConcreteResource createTestResource( String fname, String folder )
    {
        final Location loc = new SimpleLocation( "http://foo.com" );
        return new ConcreteResource( loc, String.format( "%s/%s", folder, fname ) );
    }

    private void assertLatchWait()
    {
        if ( !TestIOUtils.latchWait( latch, WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS ) )
        {
            fail( "I/O timeout" );
        }
    }

}
