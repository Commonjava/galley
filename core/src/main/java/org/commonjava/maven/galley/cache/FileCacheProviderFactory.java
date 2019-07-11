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
package org.commonjava.maven.galley.cache;

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;

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

    @SuppressWarnings( "RedundantThrows" )
    @Override
    public synchronized CacheProvider create( PathGenerator pathGenerator, TransferDecoratorManager transferDecorator,
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
