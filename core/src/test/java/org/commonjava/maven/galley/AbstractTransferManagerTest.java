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
package org.commonjava.maven.galley;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferBatch;
import org.commonjava.maven.galley.model.VirtualResource;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.commonjava.maven.galley.testing.core.transport.job.TestDownload;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractTransferManagerTest
{

    protected abstract TransferManager getTransferManagerImpl()
        throws Exception;

    protected abstract TestTransport getTransport()
        throws Exception;

    protected abstract CacheProvider getCacheProvider()
        throws Exception;

    /**
     * Addresses Issue #27 (https://github.com/Commonjava/galley/issues/27). Batches with virtual resources
     * that contain zero concrete resources should count down the latch when they return immediately, but 
     * instead are causing the countdown latch that watches the batch of transfers to hang.
     */
    @Test( timeout = 2000 )
    public void batchRetrieve_returnEmptyResultIfVirtualResourceIsEmpty()
        throws Exception
    {
        final VirtualResource vr = new VirtualResource( Collections.emptyList(), "/path/to/nowhere" );
        final TransferBatch batch =
            getTransferManagerImpl().batchRetrieve( new TransferBatch( Collections.<Resource> singleton( vr ) ), new EventMetadata() );
        assertThat( batch, notNullValue() );

        assertThat( batch.getErrors()
                         .isEmpty(), equalTo( true ) );

        assertThat( batch.getTransfers()
                         .isEmpty(), equalTo( true ) );
    }

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

    @Test( expected = TransferException.class )
    public void resourceDeletionNotAllowed() throws Exception
    {
        final String testContent = "This is a test " + System.currentTimeMillis();

        final Location loc =
                        new SimpleLocation( "test-repo", "file:///test-repo", true, false, true, true, false, false );
        final String path = "/path/to/test.txt";

        final ConcreteResource resource = new ConcreteResource( loc, path );

        // put in the content that we want to "download"
        getTransport().registerDownload( resource, new TestDownload( testContent.getBytes() ) );

        getTransferManagerImpl().delete( resource );
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
            final String result = IOUtils.toString( is, Charset.defaultCharset() );

            assertThat( result, equalTo( testContent ) );
        }
        finally
        {
            closeQuietly( is );
        }
    }

}
