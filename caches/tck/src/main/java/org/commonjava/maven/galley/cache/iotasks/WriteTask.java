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
package org.commonjava.maven.galley.cache.iotasks;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.cache.CacheProvider;

public final class WriteTask
        extends CacheProviderWorkingTask
{
    public WriteTask( CacheProvider provider, String content, ConcreteResource resource, CountDownLatch controlLatch,
                      long waiting )
    {
        super( provider, content, resource, controlLatch, waiting );
    }

    public WriteTask( CacheProvider provider, String content, ConcreteResource resource, CountDownLatch controlLatch )
    {
        super( provider, content, resource, controlLatch, -1 );
    }

    @Override
    public void run()
    {
        try
        {
            final String threadName = Thread.currentThread().getName();
            final OutputStream out = provider.openOutputStream( resource );
            final ByteArrayInputStream bais = new ByteArrayInputStream( content.getBytes() );
            int read = -1;
            final byte[] buf = new byte[512];
            System.out.println(
                    String.format( "[%s] <<<WriteTask>>> start to write to the resource with outputStream %s",
                                   threadName, out.getClass().getName() ) );
            while ( ( read = bais.read( buf ) ) > -1 )
            {
                if ( waiting > 0 )
                {
                    Thread.sleep( waiting );
                }
                out.write( buf, 0, read );
            }
            out.close();
            System.out.println(
                    String.format( "[%s] <<<WriteTask>>> writing to the resource done with outputStream %s", threadName,
                                   out.getClass().getName() ) );
            if ( controlLatch != null )
            {
                controlLatch.countDown();
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Write Task Runtime Exception." );
            e.printStackTrace();
        }
    }
}