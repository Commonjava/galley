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
package org.commonjava.maven.galley.cache.infinispan;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.io.IOException;
import java.io.InputStream;

import static org.commonjava.maven.galley.cache.infinispan.NFSOwnerCacheProducer.defineCacheConfigurations;

public class CacheTestUtil
{
    private static final String ISPN_XML = "infinispan-test.xml";

    static final String LOCAL_CACHE_FILE_NAME_FOR_TEST = "localFileCacheForTest";

    public static DefaultCacheManager getTestEmbeddedCacheManager()
    {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( ISPN_XML );

        DefaultCacheManager cacheManager = null;
        try
        {
            cacheManager = new DefaultCacheManager( stream );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        defineCacheConfigurations( cacheManager );

        // A test cache impl to do unit-testing for this local file cache expiration for purging mechanism
        Configuration configForLocalFileTest = new ConfigurationBuilder().eviction()
                                                                         .strategy( EvictionStrategy.LRU )
                                                                         .size( 1000 )
                                                                         .type( EvictionType.COUNT )
                                                                         .expiration()
                                                                         .lifespan( 1000L )
                                                                         .wakeUpInterval( 100L )
                                                                         .maxIdle( 1000L )
                                                                         .build();

        cacheManager.defineConfiguration( LOCAL_CACHE_FILE_NAME_FOR_TEST, configForLocalFileTest );

        return cacheManager;
    }
}
