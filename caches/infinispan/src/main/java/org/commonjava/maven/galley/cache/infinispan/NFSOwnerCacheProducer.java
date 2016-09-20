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

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.lookup.DummyTransactionManagerLookup;

import java.util.concurrent.TimeUnit;

/**
 * Used to create the nfs cache using pre-defined cache configuration.
 */
class NFSOwnerCacheProducer
{
    static final String CACHE_NAME = "nfs-cache";

    EmbeddedCacheManager getCacheMgr()
    {
        final EmbeddedCacheManager cacheManager = new DefaultCacheManager(
                new GlobalConfigurationBuilder().globalJmxStatistics().jmxDomain( "org.commonjava.maven.galley" ).build() );
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
