package org.commonjava.maven.galley.cache.infinispan;

import org.infinispan.Cache;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.function.Function;

/**
 * Created by jdcasey on 10/6/16.
 */
public interface CacheInstance<K, V>
{
    String getName();

    <R> R execute( Function<Cache<K, V>, R> operation );

    void stop();

    boolean containsKey( K key );

    V put( K key, V value );

    V putIfAbsent( K key, V value );

    V remove( K key );

    V get( K key );

    void beginTransaction()
            throws NotSupportedException, SystemException;

    void rollback()
                    throws SystemException;

    void commit()
                            throws SystemException, HeuristicMixedException, HeuristicRollbackException,
                                   RollbackException;

    int getTransactionStatus()
                                    throws SystemException;

    Object getLockOwner( K key );

    boolean isLocked( K key );

    void lock( K... keys );
}
