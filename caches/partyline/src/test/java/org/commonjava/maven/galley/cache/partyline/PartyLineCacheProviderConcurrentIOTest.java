/**
 * Copyright (C) 2013 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.cache.partyline;

import org.commonjava.maven.galley.cache.iotasks.ReadTask;
import org.commonjava.maven.galley.cache.iotasks.WriteTask;
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
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith( org.jboss.byteman.contrib.bmunit.BMUnitRunner.class )
@BMUnitConfig( loadDirectory = "target/test-classes/bmunit", debug = true )
public class PartyLineCacheProviderConcurrentIOTest
{

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private final String content = "This is a bmunit test";

    private final String diffContent = "This is different from content";

    private final CountDownLatch latch = new CountDownLatch( 2 );

    private PartyLineCacheProvider provider;

    private final PathGenerator pathgen = new HashedLocationPathGenerator();

    private final FileEventManager events = new TestFileEventManager();

    private final TransferDecorator decorator = new TestTransferDecorator();

    private PartyLineCacheProvider plProvider;

    private final ExecutorService executor = Executors.newFixedThreadPool( 4 );

    private final ExecutorService testPool = Executors.newFixedThreadPool( 2 );

    @Before
    public void setup()
            throws Exception
    {
        provider = new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator );

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

        TestIOUtils.latchWait( latch );

        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( content ) );
        assertThat( provider.exists( resource ), equalTo( true ) );
        assertThat( TestIOUtils.readFromStream( new FileInputStream( provider.getDetachedFile( resource ) ) ), equalTo( content ) );
    }

    @Test
    @BMScript( "TryToReadWhileWriting.btm" )
    @Ignore
    public void testBigFileReadWrite()
            throws Exception
    {
        StringBuilder builder = new StringBuilder();
        int loop = 1024;
        for ( int i = 0; i < loop; i++ )
        {
            builder.append( content );
        }
        final String bigContent = builder.toString();
        System.out.println( String.format( "the content size is: %sk", ( bigContent.length() / 1024 ) + "" ) );

        final ConcreteResource resource = createTestResource( "file_read_write_bigfile.txt" );
        testPool.execute( new WriteTask( provider, bigContent, resource, latch, 1000 ) );
        final Future<String> readingFuture =
                testPool.submit( (Callable<String>) new ReadTask( provider, content, resource, latch ) );

        TestIOUtils.latchWait( latch );

        final String readingResult = readingFuture.get();
        assertThat( readingResult, equalTo( bigContent ) );
        assertThat( provider.exists( resource ), equalTo( true ) );
        assertThat( TestIOUtils.readFromStream( new FileInputStream( provider.getDetachedFile( resource ) ) ),
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
