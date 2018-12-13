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
package org.commonjava.maven.galley.cache.iotasks;

import java.util.concurrent.CountDownLatch;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.cache.CacheProvider;

public abstract class CacheProviderWorkingTask
        implements Runnable
{
    protected CacheProvider provider;

    protected String content;

    protected ConcreteResource resource;

    protected CountDownLatch controlLatch;

    protected long waiting;

    protected CacheProviderWorkingTask( CacheProvider provider, String content, ConcreteResource resource,
                                        CountDownLatch controlLatch, long waiting )
    {
        this.provider = provider;
        this.content = content;
        this.resource = resource;
        this.controlLatch = controlLatch;
        this.waiting = waiting;
    }
}
