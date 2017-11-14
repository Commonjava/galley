/**
 * Copyright (C) 2013 Red Hat, Inc.
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

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public final class DeleteTask
        extends CacheProviderWorkingTask
        implements Callable<Boolean>
{
    private Boolean callResult;

    public DeleteTask( CacheProvider provider, String content, ConcreteResource resource, CountDownLatch controlLatch )
    {
        super( provider, content, resource, controlLatch, -1 );
    }

    public DeleteTask( CacheProvider provider, String content, ConcreteResource resource, CountDownLatch controlLatch,
                       long waiting )
    {
        super( provider, content, resource, controlLatch, waiting );
    }

    @Override
    public void run()
    {
        try
        {
            if ( waiting > 0 )
            {
                Thread.sleep( waiting );
            }
            System.out.println( "<<<DeleteTask>>> start to delete resource" );
            callResult = provider.delete( resource );
        }
        catch ( Exception e )
        {
            System.out.println( "Delete Task Runtime Exception." );
            e.printStackTrace();
        }
        finally
        {
            if(controlLatch!=null)
            {
                controlLatch.countDown();
            }
        }
    }

    @Override
    public Boolean call()
    {
        this.run();
        return callResult;
    }
}