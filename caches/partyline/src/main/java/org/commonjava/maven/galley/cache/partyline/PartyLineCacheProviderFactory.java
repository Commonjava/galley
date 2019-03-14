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
package org.commonjava.maven.galley.cache.partyline;

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.cache.CacheProviderFactory;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.util.partyline.Partyline;
import org.commonjava.util.partyline.lock.global.GlobalLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ServiceLoader;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by jdcasey on 8/30/16.
 */
public class PartyLineCacheProviderFactory
        implements CacheProviderFactory
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private File cacheDir;

    private ScheduledExecutorService deleteExecutor;

    private transient PartyLineCacheProvider provider;

    private GlobalLockManager globalLockManager;

    public PartyLineCacheProviderFactory( File cacheDir, ScheduledExecutorService deleteExecutor,
                                          GlobalLockManager globalLockManager )
    {
        this.cacheDir = cacheDir;
        this.deleteExecutor = deleteExecutor;
        this.globalLockManager = globalLockManager;
    }

    @Override
    public synchronized CacheProvider create( PathGenerator pathGenerator, TransferDecorator transferDecorator,
                                 FileEventManager fileEventManager )
            throws GalleyInitException
    {
        if ( provider == null )
        {
            Partyline fileManager;
            if (globalLockManager != null)
            {
                fileManager = new Partyline( globalLockManager );
            }
            else
            {
                fileManager = new Partyline();
            }
            provider = new PartyLineCacheProvider( cacheDir, pathGenerator, fileEventManager, transferDecorator,
                                                   deleteExecutor, fileManager );
        }

        return provider;
    }
}
