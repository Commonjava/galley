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
package org.commonjava.maven.galley.cache.routes;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Alternative;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Alternative
public class RoutingCacheProviderWrapper
        implements CacheProvider
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    private final CacheProvider safe;

    private final CacheProvider disposable;

    private final RouteSelector selector;

    public RoutingCacheProviderWrapper( final RouteSelector selector, final CacheProvider disposable,
                                        final CacheProvider safe )
    {
        this.disposable = disposable;
        this.safe = safe;
        this.selector = selector;
    }

    protected CacheProvider getRoutedProvider( ConcreteResource resource )
    {
        if ( resource != null && disposable != null )
        {
            if ( selector.isDisposable( resource ) )
            {
                logger.debug("Used disposable cache provider {}", disposable);
                return disposable;
            }
        }
        logger.debug("Used safe cache provider {}", safe);
        return safe;
    }

    @Override
    public void startReporting()
    {
        if ( disposable != null )
        {
            disposable.startReporting();
        }
        if ( safe != null )
        {
            safe.startReporting();
        }
    }

    @Override
    public void stopReporting()
    {
        if ( disposable != null )
        {
            disposable.stopReporting();
        }
        if ( safe != null )
        {
            safe.stopReporting();
        }
    }

    @Override
    public void cleanupCurrentThread()
    {
        if ( disposable != null )
        {
            disposable.cleanupCurrentThread();
        }
        if ( safe != null )
        {
            safe.cleanupCurrentThread();
        }
    }

    @Override
    public boolean isDirectory( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).isDirectory( resource );
    }

    @Override
    public boolean isFile( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).isFile( resource );
    }

    @Override
    public InputStream openInputStream( ConcreteResource resource )
            throws IOException
    {
        return getRoutedProvider( resource ).openInputStream( resource );
    }

    @Override
    public OutputStream openOutputStream( ConcreteResource resource )
            throws IOException
    {
        return getRoutedProvider( resource ).openOutputStream( resource );
    }

    @Override
    public boolean exists( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).exists( resource );
    }

    @Override
    public void copy( ConcreteResource from, ConcreteResource to )
            throws IOException
    {
        getRoutedProvider( from ).copy( from, to );
    }

    @Override
    public String getFilePath( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).getFilePath( resource );
    }

    @Override
    public boolean delete( ConcreteResource resource )
            throws IOException
    {
        return getRoutedProvider( resource ).delete( resource );
    }

    @Override
    public String[] list( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).list( resource );
    }

    @Override
    public void mkdirs( ConcreteResource resource )
            throws IOException
    {
        getRoutedProvider( resource ).mkdirs( resource );
    }

    @Override
    public void createFile( ConcreteResource resource )
            throws IOException
    {
        getRoutedProvider( resource ).createFile( resource );
    }

    @Override
    public void createAlias( ConcreteResource from, ConcreteResource to )
            throws IOException
    {
        getRoutedProvider( from ).createAlias( from, to );
    }

    @Override
    public Transfer getTransfer( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).getTransfer( resource );
    }

    @Override
    public void clearTransferCache()
    {
        if ( disposable != null )
        {
            disposable.clearTransferCache();
        }
        if ( safe != null )
        {
            safe.clearTransferCache();
        }
    }

    @Override
    public long length( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).length( resource );
    }

    @Override
    public long lastModified( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).lastModified( resource );
    }

    @Override
    public boolean isReadLocked( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).isReadLocked( resource );
    }

    @Override
    public boolean isWriteLocked( ConcreteResource resource )
    {
        return getRoutedProvider( resource ).isWriteLocked( resource );
    }

    @Override
    public void unlockRead( ConcreteResource resource )
    {
        getRoutedProvider( resource ).unlockRead( resource );
    }

    @Override
    public void unlockWrite( ConcreteResource resource )
    {
        getRoutedProvider( resource ).unlockWrite( resource );
    }

    @Override
    public void lockRead( ConcreteResource resource )
    {
        getRoutedProvider( resource ).lockRead( resource );
    }

    @Override
    public void lockWrite( ConcreteResource resource )
    {
        getRoutedProvider( resource ).lockWrite( resource );
    }

    @Override
    public void waitForWriteUnlock( ConcreteResource resource )
    {
        getRoutedProvider( resource ).waitForWriteUnlock( resource );
    }

    @Override
    public void waitForReadUnlock( ConcreteResource resource )
    {
        getRoutedProvider( resource ).waitForReadUnlock( resource );
    }

    @Override
    public AdminView asAdminView()
    {
        throw new UnsupportedOperationException( "No support for AdminView" );
    }
}
