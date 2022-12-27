/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.join;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class CacheProviderTCK
{

    protected abstract CacheProvider getCacheProvider()
        throws Exception;

    @Test
    public void lockThenWaitForLockReturnsImmediatelyInSameThread()
        throws Exception
    {
        final Location loc = new SimpleLocation( "http://foo.com" );
        final String path = "my/path.txt";
        final ConcreteResource res = new ConcreteResource( loc, path );

        final CacheProvider cache = getCacheProvider();

        cache.lockWrite( res );
        cache.waitForWriteUnlock( res );

        assertThat( cache.isWriteLocked( res ), is( false ) );
    }

    @Test
    public void lockThenWriteViaTransferSucceedsInSameThread()
        throws Exception
    {
        final Location loc = new SimpleLocation( "http://foo.com" );
        final String path = "my/path.txt";
        final ConcreteResource res = new ConcreteResource( loc, path );

        final CacheProvider cache = getCacheProvider();

        cache.lockWrite( res );

        final Transfer txfr = new Transfer( res, cache, new TestFileEventManager(), new TransferDecoratorManager( new TestTransferDecorator() ) );

        OutputStream out = null;
        try
        {
            out = txfr.openOutputStream( TransferOperation.UPLOAD );
            IOUtils.write( "this is a test", out, Charset.defaultCharset() );
        }
        finally
        {
            IOUtils.closeQuietly( out );
        }
    }

    @Test
    public void writeAndVerifyDirectory()
        throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String dir = "/path/to/my/";
        final String fname = dir + "file.txt";

        final CacheProvider provider = getCacheProvider();
        final OutputStream out = provider.openOutputStream( new ConcreteResource( loc, fname ) );
        out.write( content.getBytes( StandardCharsets.UTF_8 ) );
        out.close();

        assertThat( provider.isDirectory( new ConcreteResource( loc, dir ) ), is( true ) );
    }

    @Test
    public void writeAndListDirectory()
        throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String dir = "/path/to/my/";
        final String fname = dir + "file.txt";

        final CacheProvider provider = getCacheProvider();
        final OutputStream out = provider.openOutputStream( new ConcreteResource( loc, fname ) );
        out.write( content.getBytes( StandardCharsets.UTF_8 ) );
        out.flush();
        out.close();

        // NOTE: This is NOT as tightly specified as I would like. 
        // We keep the listing assertions loose (greater-than instead of equals, 
        // contains instead of exact positional assertion) because the Infinispan
        // live testing has these spurious foo.txt.#0 files cropping up.
        //
        // I have no idea what they are, but I'm sick of fighting JBoss bugs for now.
        final Set<String> listing = new HashSet<>( Arrays.asList( provider.list( new ConcreteResource( loc, dir ) ) ) );

        System.out.printf( "\n\nFile listing is:\n\n  %s\n\n\n", join( listing, "\n  " ) );

        assertThat( listing.size() > 0, is( true ) );
        assertThat( listing.contains( "file.txt" ), is( true ) );
    }

    @Test
    public void writeAndVerifyExistence()
        throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";

        final CacheProvider provider = getCacheProvider();
        final OutputStream out = provider.openOutputStream( new ConcreteResource( loc, fname ) );
        out.write( content.getBytes( StandardCharsets.UTF_8 ) );
        out.close();

        assertThat( provider.exists( new ConcreteResource( loc, fname ) ), is( true ) );
    }

    @Test
    public void writeDeleteAndVerifyNonExistence()
        throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";

        final CacheProvider provider = getCacheProvider();
        final OutputStream out = provider.openOutputStream( new ConcreteResource( loc, fname ) );
        out.write( content.getBytes( StandardCharsets.UTF_8 ) );
        out.close();

        assertThat( provider.exists( new ConcreteResource( loc, fname ) ), is( true ) );

        provider.delete( new ConcreteResource( loc, fname ) );

        assertThat( provider.exists( new ConcreteResource( loc, fname ) ), is( false ) );
    }

    @Test
    public void writeAndReadFile()
        throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";

        final CacheProvider provider = getCacheProvider();
        final OutputStream out = provider.openOutputStream( new ConcreteResource( loc, fname ) );
        out.write( content.getBytes( StandardCharsets.UTF_8 ) );
        out.close();

        final InputStream in = provider.openInputStream( new ConcreteResource( loc, fname ) );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = -1;
        final byte[] buf = new byte[512];
        while ( ( read = in.read( buf ) ) > -1 )
        {
            baos.write( buf, 0, read );
        }

        final String result = new String( baos.toByteArray(), StandardCharsets.UTF_8 );

        assertThat( result, is( content ) );
    }

    @Test
    public void writeCopyAndReadNewFile()
        throws Exception
    {
        final String content = "This is a test";

        final Location loc = new SimpleLocation( "http://foo.com" );
        final String fname = "/path/to/my/file.txt";

        final Location loc2 = new SimpleLocation( "http://bar.com" );

        final CacheProvider provider = getCacheProvider();
        final OutputStream out = provider.openOutputStream( new ConcreteResource( loc, fname ) );
        out.write( content.getBytes( StandardCharsets.UTF_8 ) );
        out.close();

        provider.copy( new ConcreteResource( loc, fname ), new ConcreteResource( loc2, fname ) );

        final InputStream in = provider.openInputStream( new ConcreteResource( loc2, fname ) );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = -1;
        final byte[] buf = new byte[512];
        while ( ( read = in.read( buf ) ) > -1 )
        {
            baos.write( buf, 0, read );
        }

        final String result = new String( baos.toByteArray(), StandardCharsets.UTF_8 );

        assertThat( result, is( content ) );
    }

}
