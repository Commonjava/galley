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
package org.commonjava.maven.galley;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.commonjava.maven.galley.testing.core.transport.job.TestDownload;
import org.junit.Test;

public abstract class AbstractTransferManagerTest
{

    protected abstract TransferManagerImpl getTransferManagerImpl()
        throws Exception;

    protected abstract TestTransport getTransport()
        throws Exception;

    protected abstract CacheProvider getCacheProvider()
        throws Exception;

    /**
     * Test that cached content will be used...if not, this test will fail, as 
     * the wrong content is registered with the test transport.
     */
    @Test
    public void retrieve_preferCachedCopy()
        throws Exception
    {
        final String testContent = "This is a test " + System.currentTimeMillis();

        final Location loc = new SimpleLocation( "file:///test-repo" );
        final String path = "/path/to/test.txt";

        final ConcreteResource resource = new ConcreteResource( loc, path );

        // put in some wrong content that will cause problems if the cache isn't used.
        getTransport().registerDownload( resource, new TestDownload( "This is WRONG".getBytes() ) );

        // seed the cache with the file we're trying to retrieve.
        OutputStream os = null;
        try
        {
            os = getCacheProvider().openOutputStream( resource );
            os.write( testContent.getBytes() );
        }
        finally
        {
            closeQuietly( os );
        }

        // now, use the manager to retrieve() the path...the cached content should come through here.
        final Transfer transfer = getTransferManagerImpl().retrieve( resource );

        assertTransferContent( transfer, testContent );
    }

    /**
     * Test that remote content will be downloaded then cached.
     */
    @Test
    public void retrieve_cacheIfMissing()
        throws Exception
    {
        final String testContent = "This is a test " + System.currentTimeMillis();

        final Location loc = new SimpleLocation( "file:///test-repo" );
        final String path = "/path/to/test.txt";

        final ConcreteResource resource = new ConcreteResource( loc, path );

        // put in the content that we want to "download"
        getTransport().registerDownload( resource, new TestDownload( testContent.getBytes() ) );

        // now, use the manager to retrieve() the path...the remote content should come through here.
        Transfer transfer = getTransferManagerImpl().retrieve( resource );

        assertTransferContent( transfer, testContent );

        // now, the right content should be cached.
        // So, we'll put in some wrong content that will cause problems if the cache isn't used.
        getTransport().registerDownload( resource, new TestDownload( "This is WRONG".getBytes() ) );

        // now, use the manager to retrieve() the path again...the cached content should come through here.
        transfer = getTransferManagerImpl().retrieve( resource );

        assertTransferContent( transfer, testContent );
    }

    private void assertTransferContent( final Transfer transfer, final String testContent )
        throws Exception
    {
        // if this is null, it's a sign that the manager tried to retrieve the content remotely and ignored the cache.
        assertThat( transfer, notNullValue() );

        // now, read the content to verify it matches what we wrote above.
        InputStream is = null;
        try
        {
            is = transfer.openInputStream();
            final String result = IOUtils.toString( is );

            assertThat( result, equalTo( testContent ) );
        }
        finally
        {
            closeQuietly( is );
        }
    }

}
