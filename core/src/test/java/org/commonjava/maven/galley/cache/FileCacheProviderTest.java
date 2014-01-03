/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.cache;

import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

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
                                      new NoOpTransferDecorator(), true );
    }

}
