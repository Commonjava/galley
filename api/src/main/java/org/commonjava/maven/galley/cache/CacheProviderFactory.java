package org.commonjava.maven.galley.cache;

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

/**
 * Created by jdcasey on 8/30/16.
 */
public interface CacheProviderFactory
{
    CacheProvider create( PathGenerator pathGenerator, TransferDecorator transferDecorator,
                          FileEventManager fileEventManager )
            throws GalleyInitException;
}
