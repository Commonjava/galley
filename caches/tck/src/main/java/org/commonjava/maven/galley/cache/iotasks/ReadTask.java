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
package org.commonjava.maven.galley.cache.iotasks;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.cache.CacheProvider;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public final class ReadTask
        extends CacheProviderWorkingTask
        implements Callable<String>
{
    private String readingResult;

    public ReadTask( CacheProvider provider, String content, ConcreteResource resource, CountDownLatch controlLatch )
    {
        super( provider, content, resource, controlLatch, -1 );
    }

    public ReadTask( CacheProvider provider, String content, ConcreteResource resource, CountDownLatch controlLatch,
                     long waiting )
    {
        super( provider, content, resource, controlLatch, waiting );
    }

    @Override
    public void run()
    {
        try
        {
            final String threadName = Thread.currentThread().getName();
            final InputStream in = provider.openInputStream( resource );
            if ( in == null )
            {
                System.out.println( "Can not read content as the input stream is null." );
                if ( controlLatch != null )
                {
                    controlLatch.countDown();
                }
                return;
            }
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            final byte[] buf = new byte[512];
            System.out.printf( "[%s] <<<ReadTask>>> will start to read from the resource with inputStream %s%n",
                               threadName, in.getClass().getName() );
            while ( ( read = in.read( buf ) ) > -1 )
            {
                if ( waiting > 0 )
                {
                    //noinspection BusyWait
                    Thread.sleep( waiting );
                }
                System.out.println(">>> " + read );
                baos.write( buf, 0, read );
            }
            baos.close();
            in.close();
            System.out.printf( "[%s] <<<ReadTask>>> reading from the resource done with inputStream %s%n", threadName,
                               in.getClass().getName() );
            readingResult = new String( baos.toByteArray(), StandardCharsets.UTF_8 );
            if ( controlLatch != null )
            {
                controlLatch.countDown();
                System.out.println("Count down read task, controlLatch=" + controlLatch.getCount() );
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Read Task Runtime Exception." );
            e.printStackTrace();
        }
    }

    @Override
    public String call()
    {
        this.run();
        return readingResult;
    }
}