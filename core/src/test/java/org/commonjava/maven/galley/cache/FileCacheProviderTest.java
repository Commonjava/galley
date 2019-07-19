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

import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.OutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FileCacheProviderTest
    extends CacheProviderTCK
{

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Override
    protected CacheProvider getCacheProvider()
        throws Exception
    {
        return new FileCacheProvider( temp.newFolder( "cache" ), new HashedLocationPathGenerator(), new NoOpFileEventManager(),
                                      new TransferDecoratorManager( new NoOpTransferDecorator() ), true );
    }


    @Test
    public void testGetDetachedFile()
                    throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";
        final ConcreteResource resource = new ConcreteResource( loc, fname );
        final CacheProvider provider = getCacheProvider();

        final OutputStream out = provider.openOutputStream( resource );
        out.write( content.getBytes( "UTF-8" ) );
        out.close();

        File file = provider.asAdminView().getDetachedFile( resource );

        assertThat( provider.exists( resource ), equalTo( true ) );
        assertTrue( file.exists() );
    }
}
