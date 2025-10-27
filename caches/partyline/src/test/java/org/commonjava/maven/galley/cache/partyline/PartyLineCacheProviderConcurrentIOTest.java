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
package org.commonjava.maven.galley.cache.partyline;

import org.commonjava.maven.galley.cache.MockPathGenerator;
import org.commonjava.maven.galley.cache.iotasks.ReadTask;
import org.commonjava.maven.galley.cache.iotasks.WriteTask;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestIOUtils;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.util.partyline.Partyline;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@Ignore
@RunWith( org.jboss.byteman.contrib.bmunit.BMUnitRunner.class )
@BMUnitConfig( loadDirectory = "target/test-classes/bmunit", debug = true )
public class PartyLineCacheProviderConcurrentIOTest
{

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private final String content = "Testing";

    private CountDownLatch latch;

    private PartyLineCacheProvider provider;

    private final PathGenerator pathgen = new MockPathGenerator();

    private final FileEventManager events = new TestFileEventManager();

    private final TransferDecorator decorator = new TestTransferDecorator();

    private final ExecutorService testPool = Executors.newFixedThreadPool( 2 );

    private final long WAIT_TIMEOUT_SECONDS = 300;

    @Before
    public void setup()
            throws Exception
    {
        provider = new PartyLineCacheProvider( temp.newFolder(), pathgen, events, new TransferDecoratorManager( decorator ),
                                               Executors.newScheduledThreadPool( 2 ), new Partyline() );
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
        assertThat( TestIOUtils.readFromStream( Files.newInputStream( provider.getDetachedFile( resource ).toPath() ) ),
                    equalTo( content ) );
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
        System.out.printf( "the content size is: %dm%n", bigContent.length() / 1024 / 1024 );

        final ConcreteResource resource = createTestResource( "file_read_write_bigfile.txt" );
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
        assertThat( TestIOUtils.readFromStream( Files.newInputStream( provider.getDetachedFile( resource ).toPath() ) ),
                    equalTo( bigContent ) );
    }

    @Test
    public void testBigFileWrite()
            throws Exception
    {
        StringBuilder builder = new StringBuilder();
        int loop = 1024 * 1024 * 10;
        for ( int i = 0; i < loop; i++ )
        {
            builder.append( content );
        }
        final String bigContent = builder.toString();
        System.out.printf( "the content size is: %dm%n", bigContent.length() / 1024 / 1024 );

        final ConcreteResource resource = createTestResource( "file_read_write_bigfile.txt" );
        new WriteTask( provider, bigContent, resource, null ).run();

        assertThat( provider.exists( resource ), equalTo( true ) );
        assertThat( TestIOUtils.readFromStream( Files.newInputStream( provider.getDetachedFile( resource ).toPath() ) ),
                    equalTo( bigContent ) );
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

}
