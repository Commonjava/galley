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

import org.commonjava.maven.galley.cache.CacheProviderTCK;
import org.commonjava.maven.galley.cache.MockPathGenerator;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.util.partyline.JoinableFileManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.util.concurrent.Executors;

public class PartyLineCacheProviderTest
    extends CacheProviderTCK
{

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private PartyLineCacheProvider provider;

    @Before
    public void setup()
        throws Exception
    {
        final PathGenerator pathgen = new MockPathGenerator();
        final FileEventManager events = new TestFileEventManager();
        final TransferDecorator decorator = new TestTransferDecorator();

        provider = new PartyLineCacheProvider( temp.newFolder(), pathgen, events, new TransferDecoratorManager( decorator ),
                                               Executors.newScheduledThreadPool( 2 ), new JoinableFileManager() );
    }

    @Override
    protected CacheProvider getCacheProvider()
    {
        return provider;
    }
}
