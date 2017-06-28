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
package org.commonjava.maven.galley.cache.infinispan;

import org.infinispan.Cache;
import org.infinispan.util.concurrent.locks.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Holder class that helps manage the shutdown process for things that use Infinispan.
 */
public class SimpleCacheInstance<K,V>
        implements CacheInstance<K, V>
{
    private String name;

    private Cache<K,V> cache;

    private boolean stopped;

    protected SimpleCacheInstance(){}

    public SimpleCacheInstance( String named, Cache<K, V> cache )
    {
        this.name = named;
        this.cache = cache;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public <R> R execute( Function<Cache<K, V>, R> operation )
    {
        if ( !stopped )
        {
            try
            {
                return operation.apply( cache );
            }
            catch ( RuntimeException e )
            {
                // this may happen if the cache is in the process of shutting down
                Logger logger = LoggerFactory.getLogger( getClass() );
                logger.error( "Failed to complete operation: " + e.getMessage(), e );
            }
        }
        else
        {
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.error( "Cannot complete operation. Cache {} is shutting down.", name );
        }

        return null;
    }

    @Override
    public void stop()
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Cache {} is shutting down!", name );
        this.stopped = true;
    }

    @Override
    public boolean containsKey( K key )
    {
        return execute( cache -> cache.containsKey( key ) );
    }

    @Override
    public V put( K key, V value )
    {
        return execute( cache -> cache.put( key, value ) );
    }

    @Override
    public V putIfAbsent( K key, V value )
    {
        return execute( ( c ) -> c.putIfAbsent( key, value ) );
    }

    @Override
    public V remove( K key )
    {
        return execute( cache -> cache.remove( key ) );
    }

    @Override
    public V get( K key )
    {
        return execute( cache -> cache.get( key ) );
    }

    @Override
    public void beginTransaction()
            throws NotSupportedException, SystemException
    {
        AtomicReference<NotSupportedException> suppEx = new AtomicReference<>();
        AtomicReference<SystemException> sysEx = new AtomicReference<>();
        execute( ( c ) -> {
            try
            {
                c.getAdvancedCache().getTransactionManager().begin();
            }
            catch ( NotSupportedException e )
            {
                suppEx.set( e );
            }
            catch ( SystemException e )
            {
                sysEx.set( e );
            }

            return null;
        } );

        if ( suppEx.get() != null )
        {
            throw suppEx.get();
        }

        if ( sysEx.get() != null )
        {
            throw sysEx.get();
        }
    }

    @Override
    public void rollback()
            throws SystemException
    {
        AtomicReference<SystemException> sysEx = new AtomicReference<>();
        execute( ( c ) -> {
            try
            {
                c.getAdvancedCache().getTransactionManager().rollback();
            }
            catch ( SystemException e )
            {
                sysEx.set( e );
            }

            return null;
        } );


        if ( sysEx.get() != null )
        {
            throw sysEx.get();
        }
    }

    @Override
    public void commit()
            throws SystemException, HeuristicMixedException, HeuristicRollbackException, RollbackException
    {
        AtomicReference<SystemException> sysEx = new AtomicReference<>();
        AtomicReference<HeuristicMixedException> hmEx = new AtomicReference<>();
        AtomicReference<HeuristicRollbackException> hrEx = new AtomicReference<>();
        AtomicReference<RollbackException> rEx = new AtomicReference<>();
        execute( ( c ) -> {
            try
            {
                c.getAdvancedCache().getTransactionManager().commit();
            }
            catch ( SystemException e )
            {
                sysEx.set( e );
            }
            catch ( HeuristicMixedException e )
            {
                hmEx.set( e );
            }
            catch ( HeuristicRollbackException e )
            {
                hrEx.set( e );
            }
            catch ( RollbackException e )
            {
                rEx.set( e );
            }

            return null;
        } );

        if ( sysEx.get() != null )
        {
            throw sysEx.get();
        }

        if ( hmEx.get() != null )
        {
            throw hmEx.get();
        }

        if ( hrEx.get() != null )
        {
            throw hrEx.get();
        }

        if ( rEx.get() != null )
        {
            throw rEx.get();
        }
    }

    @Override
    public int getTransactionStatus()
            throws SystemException
    {
        AtomicReference<SystemException> sysEx = new AtomicReference<>();

        Integer result = execute( ( c ) -> {
            try
            {
                return c.getAdvancedCache().getTransactionManager().getStatus();
            }
            catch ( SystemException e )
            {
                sysEx.set( e );
            }

            return null;
        } );

        if ( sysEx.get() != null )
        {
            throw sysEx.get();
        }

        return result;
    }

    @Override
    public Object getLockOwner( K key )
    {
        return execute( ( c ) -> c.getAdvancedCache().getLockManager().getOwner( key ) );
    }

    @Override
    public boolean isLocked( K key )
    {
        return execute( ( c ) -> c.getAdvancedCache().getLockManager().isLocked( key ) );
    }

    @Override
    public void lock( K... keys )
    {
        execute( ( c ) -> c.getAdvancedCache().lock( keys ) );
    }

    @Override
    public void unlock( K key )
    {
        execute( ( c ) ->
                 {
                     try
                     {
                         LockManager mgr = c.getAdvancedCache().getLockManager();
                         Object owner = mgr.getOwner( key );
                         c.getAdvancedCache().getLockManager().unlock( key, owner );
                         return true;
                     }
                     catch ( RuntimeException e )
                     {
			 Logger logger = LoggerFactory.getLogger( SimpleCacheInstance.this.getClass() );
			 logger.error( String.format( "Failed to unlock key %s: %s", key, e.getMessage() ), e );
                         return false;
                     }
                 } );
    }

}
