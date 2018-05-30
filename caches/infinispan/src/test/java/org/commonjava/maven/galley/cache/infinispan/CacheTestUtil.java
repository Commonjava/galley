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
