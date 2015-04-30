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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLockingSupport
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Map<ConcreteResource, WeakReference<Thread>> lock =
        new HashMap<ConcreteResource, WeakReference<Thread>>();

    public void waitForUnlock( final ConcreteResource resource )
    {
        synchronized ( lock )
        {
            while ( isLocked( resource ) )
            {
                try
                {
                    lock.wait();
                }
                catch ( final InterruptedException e )
                {
                    // TODO
                }
            }
        }
    }

    public boolean isLocked( final ConcreteResource resource )
    {
        return lock.containsKey( resource );
    }

    public void unlock( final ConcreteResource resource )
    {
        synchronized ( lock )
        {
            lock.remove( resource );
            lock.notifyAll();
        }
    }

    public void lock( final ConcreteResource resource )
    {
        synchronized ( lock )
        {
            lock.put( resource, new WeakReference<Thread>( Thread.currentThread() ) );
            lock.notifyAll();
        }
    }

    public synchronized void cleanupCurrentThread()
    {
        final long id = Thread.currentThread()
                              .getId();
        for ( final ConcreteResource res : new HashSet<ConcreteResource>( lock.keySet() ) )
        {
            final WeakReference<Thread> ref = lock.get( res );
            if ( ref != null )
            {
                boolean rm = false;
                if ( ref.get() == null )
                {
                    logger.debug( "Cleaning up lock: {} for thread: {}", res, Thread.currentThread()
                                                                                    .getName() );
                    rm = true;
                }
                else if ( ref.get()
                             .getId() == id )
                {
                    logger.debug( "Cleaning up lock: {} for thread: {}", res, Thread.currentThread()
                                                                                    .getName() );
                    rm = true;
                }

                if ( rm )
                {
                    synchronized ( lock )
                    {
                        lock.remove( res );
                        lock.notifyAll();
                    }
                }
            }
        }
    }

}
