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
package org.commonjava.maven.galley.cache;

import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractFileCacheBMUnitTest
{
    protected final String content = "This is a bmunit test";

    protected final Location loc = new SimpleLocation( "http://foo.com" );

    protected final String fname = "/path/to/my/file.txt";

    protected final ConcreteResource resource = new ConcreteResource( loc, fname );

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    protected final CountDownLatch latch = new CountDownLatch( 2 );

    protected CacheProvider provider;

    protected String result = null;

    @Before
    public void getCacheProvider()
            throws Exception
    {
        temp.create();
        provider = new FileCacheProvider( temp.newFolder( "cache" ), new MockPathGenerator(),
                                          new NoOpFileEventManager(), new TransferDecoratorManager( new NoOpTransferDecorator() ), true );
    }

    protected void start( Runnable r )
    {
        new Thread( r ).start();
    }

    protected class WriteThread
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
                    //noinspection BusyWait
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

    protected class ReadThread
            implements Runnable
    {
        @Override
        public void run()
        {
            try(final InputStream in = provider.openInputStream( resource ))
            {
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
                result = new String( baos.toByteArray(), StandardCharsets.UTF_8 );
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
