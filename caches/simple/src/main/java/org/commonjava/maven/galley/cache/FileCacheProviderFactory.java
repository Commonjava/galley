package org.commonjava.maven.galley.cache;

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

import java.io.File;

/**
 * Created by jdcasey on 8/30/16.
 */
public class FileCacheProviderFactory
        implements CacheProviderFactory
{
    private File cacheDir;

    private transient FileCacheProvider provider;

    public FileCacheProviderFactory( File cacheDir )
    {
        this.cacheDir = cacheDir;
    }

    @Override
    public synchronized CacheProvider create( PathGenerator pathGenerator, TransferDecorator transferDecorator,
                                 FileEventManager fileEventManager )
            throws GalleyInitException
    {
        if ( provider == null )
        {
            provider = new FileCacheProvider( cacheDir, pathGenerator, fileEventManager, transferDecorator );
        }

        return provider;
    }
}
