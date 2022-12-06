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
package org.commonjava.maven.galley.io.checksum;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.io.checksum.testutil.TestMetadataConsumer;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.testing.core.ApiFixture;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.commonjava.maven.galley.io.checksum.ContentDigest.MD5;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ChecksummingOutputStreamTest
{

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public final ApiFixture fixture = new ApiFixture( temp );

    @Before
    public void before()
    {
        fixture.initMissingComponents();
        fixture.getCache().startReporting();
    }

    @Test
    public void verifyUsingMd5()
            throws Exception
    {
        final Transfer txfr = fixture.getCache()
                                     .getTransfer(
                                             new ConcreteResource( new SimpleLocation( "test:uri" ), "my-path.txt" ) );
        final OutputStream os = new ByteArrayOutputStream();

        final byte[] data =
                "This is a test with a bunch of data and some other stuff, in a big box sealed with chewing gum".getBytes();

        ChecksummingOutputStream stream = null;
        final TestMetadataConsumer testConsumer = new TestMetadataConsumer();
        try
        {
            stream = new ChecksummingOutputStream( new HashSet<>(
                    Arrays.asList( new Md5GeneratorFactory(), new Sha1GeneratorFactory(),
                                   new Sha256GeneratorFactory() ) ), os, txfr, testConsumer, true, null );

            stream.write( data );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }

        final MessageDigest md = MessageDigest.getInstance( "MD5" );
        md.update( data );
        final byte[] digest = md.digest();
        final String digestHex = Hex.encodeHexString( digest );

        final Transfer md5Txfr = txfr.getSiblingMeta( ".md5" );
        InputStream in = null;
        String resultHex = null;
        try
        {
            in = md5Txfr.openInputStream();

            resultHex = IOUtils.toString( in, Charset.defaultCharset() );
        }
        finally
        {
            IOUtils.closeQuietly( in );
        }

        assertThat( resultHex, equalTo( digestHex ) );

        TransferMetadata metadata = testConsumer.getMetadata( txfr );
        assertThat( metadata, notNullValue() );

        Map<ContentDigest, String> digests = metadata.getDigests();
        assertThat( digests, CoreMatchers.notNullValue() );
        assertThat( digests.get( MD5 ), equalTo( digestHex ) );
    }

}
