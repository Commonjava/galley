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
package org.commonjava.maven.galley.cache;

import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith( org.jboss.byteman.contrib.bmunit.BMUnitRunner.class )
@BMUnitConfig( loadDirectory = "target/test-classes/bmunit", debug = true )
public class FileCacheBMUnitTest
{

    final String content = "This is a bmunit test";

    final Location loc = new SimpleLocation( "http://foo.com" );

    final String fname = "/path/to/my/file.txt";

    final CountDownLatch latch = new CountDownLatch( 2 );

    final ConcreteResource resource = new ConcreteResource( loc, fname );

    private CacheProvider provider;

    private String result = null;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void getCacheProvider()
            throws Exception
    {
        temp.create();
        provider = new FileCacheProvider( temp.newFolder( "cache" ), new HashedLocationPathGenerator(),
                                          new NoOpFileEventManager(), new NoOpTransferDecorator(), true );
    }


    @Test
    @BMScript( "TryToReadWhileWritingTestCase.btm" )
    @Ignore( "Needs to be in a separate class to avoid BindException on byteman JVM agent")
    public void testTryToReadWhileWriting()
    {
        new Thread( new WriteThread() ).start();
        new Thread( new ReadThread() ).start();
        try
        {
            latch.await();
        }
        catch ( Exception e )
        {
            System.out.println( "Threads await Exception." );
        }
        assertThat( result, equalTo( content ) );
    }

    @Test
    @BMScript( "TryToWriteWhileReadingTestCase.btm" )
    @Ignore( "Needs to be in a separate class to avoid BindException on byteman JVM agent")
    public void testTryToWriteWhileReading()
    {
        new Thread( new ReadThread() ).start();
        new Thread( new WriteThread() ).start();
        try
        {
            latch.await();
        }
        catch ( Exception e )
        {
            System.out.println( "Threads await Exception." );
        }
        assertNull( result );
    }

    class WriteThread
            implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                final OutputStream out = provider.openOutputStream( resource );
                final ByteArrayInputStream bais = new ByteArrayInputStream( content.getBytes() );
                int read = -1;
                final byte[] buf = new byte[512];
                while ( ( read = bais.read( buf ) ) > -1 )
                {
                    Thread.sleep( 1000 );
                    out.write( buf, 0, read );
                }
                out.close();
                latch.countDown();
            }
            catch ( Exception e )
            {
                System.out.println( "Write Thread Runtime Exception." );
                e.printStackTrace();
            }
            finally
            {
                System.out.println( "<<<write: start unlocking write" );
                provider.unlockWrite( resource );
                System.out.println( "<<<write: finish unlocking write" );
            }
        }
    }

    class ReadThread
            implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                final InputStream in = provider.openInputStream( resource );
                if ( in == null )
                {
                    latch.countDown();
                    return;
                }
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int read = -1;
                final byte[] buf = new byte[512];
                while ( ( read = in.read( buf ) ) > -1 )
                {
                    baos.write( buf, 0, read );
                }
                result = new String( baos.toByteArray(), "UTF-8" );
                latch.countDown();
            }
            catch ( Exception e )
            {
                System.out.println( "Read Thread Runtime Exception." );
                e.printStackTrace();
            }
            finally
            {
                System.out.println( "<<<read: start unlocking read" );
                provider.unlockRead( resource );
                System.out.println( "<<<read: finish unlocking read" );
            }
        }
    }
}