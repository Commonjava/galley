package org.commonjava.maven.galley.cache.infinispan;

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.cache.CacheProviderFactory;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.infinispan.Cache;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * Created by jdcasey on 8/30/16.
 */
public class FastLocalCacheProviderFactory
        implements CacheProviderFactory
{
    private File cacheDir;

    private final File nfsDir;

    private Cache<String, String> nfsUsageCache;

    private final ExecutorService executor;

    private transient FastLocalCacheProvider provider;

    public FastLocalCacheProviderFactory( File cacheDir, File nfsDir, Cache<String, String> nfsUsageCache, ExecutorService executor )
    {
        this.cacheDir = cacheDir;
        this.nfsDir = nfsDir;
        this.nfsUsageCache = nfsUsageCache;
        this.executor = executor;
    }

    @Override
    public synchronized CacheProvider create( PathGenerator pathGenerator, TransferDecorator transferDecorator,
                                 FileEventManager fileEventManager )
            throws GalleyInitException
    {
        if ( provider == null )
        {
            PartyLineCacheProvider pl =
                    new PartyLineCacheProvider( cacheDir, pathGenerator, fileEventManager, transferDecorator );

            provider =
                    new FastLocalCacheProvider( pl, nfsUsageCache, pathGenerator, fileEventManager, transferDecorator,
                                                executor, nfsDir.getPath() );
        }

        return provider;
    }
}
