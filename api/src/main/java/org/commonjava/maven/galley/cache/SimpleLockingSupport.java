package org.commonjava.maven.galley.cache;

import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.galley.model.ConcreteResource;

public class SimpleLockingSupport
{

    private final Set<ConcreteResource> lock = new HashSet<ConcreteResource>();

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
        return lock.contains( resource );
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
            lock.add( resource );
            lock.notifyAll();
        }
    }

}
