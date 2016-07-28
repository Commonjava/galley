package org.commonjava.maven.galley.cache.infinispan;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.infinispan.Cache;
import org.infinispan.cdi.ConfigureCache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.EvictionConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.lookup.DummyTransactionManagerLookup;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import java.util.concurrent.TimeUnit;

/**
 * Used to create the nfs cache using pre-defined cache configuration.
 */
@ApplicationScoped
public class NFSOwnerCacheProducer
{
    static final String CACHE_NAME = "nfs-cache";

    @ConfigureCache( CACHE_NAME )
    @NFSOwnerCache
    @Produces
    @ApplicationScoped
    public Configuration getCacheCfg()
    {
        return getCacheMgr().getCacheConfiguration( CACHE_NAME );
    }

    protected EmbeddedCacheManager getCacheMgr()
    {
        final EmbeddedCacheManager cacheManager = new DefaultCacheManager(
                new GlobalConfigurationBuilder().globalJmxStatistics().jmxDomain( "org.commonjava" ).build() );
        // Want to enable dead lock check as lock will be used in FastLocalCacheProvider
        // and also set transaction mode to PESSIMISTIC with DummyTransactionManger.
        final Configuration configuration = new ConfigurationBuilder().eviction()
                                                                      .strategy( EvictionStrategy.LRU )
                                                                      .size( 1000 )
                                                                      .type( EvictionType.COUNT )
                                                                      .deadlockDetection()
                                                                      .spinDuration( 10, TimeUnit.SECONDS )
                                                                      .enable()
                                                                      .transaction()
                                                                      .transactionManagerLookup(
                                                                              new DummyTransactionManagerLookup() )
                                                                      .lockingMode( LockingMode.PESSIMISTIC )
                                                                      .build();
        cacheManager.defineConfiguration( CACHE_NAME, configuration );
        return cacheManager;
    }
}
