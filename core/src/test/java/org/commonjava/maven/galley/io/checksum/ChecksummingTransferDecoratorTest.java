/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.ChecksummingTransferDecorator;
import org.commonjava.maven.galley.io.SpecialPathManagerImpl;
import org.commonjava.maven.galley.io.checksum.testutil.TestMetadataConsumer;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.testing.core.ApiFixture;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static org.commonjava.maven.galley.io.ChecksummingTransferDecorator.FORCE_CHECKSUM;
import static org.commonjava.maven.galley.io.checksum.ContentDigest.MD5;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by jdcasey on 4/27/17.
 */
public class ChecksummingTransferDecoratorTest
{
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public ApiFixture fixture = new ApiFixture( temp );

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private TestMetadataConsumer metadataConsumer = new TestMetadataConsumer();

    @Before
    public void before()
    {
        fixture.setDecorator( new ChecksummingTransferDecorator( Collections.<TransferOperation>emptySet(),
                                                                 new SpecialPathManagerImpl(), false, false,
                                                                 metadataConsumer, new Md5GeneratorFactory() ) );
        fixture.initMissingComponents();
        fixture.getCache().startReporting();
    }

    @Test
    public void forceChecksumOnReadWhenChecksumsAreDisabledForReads()
            throws Exception
    {
        String path = "my-path.txt";
        final Transfer txfr = fixture.getCache()
                                     .getTransfer(
                                             new ConcreteResource( new SimpleLocation( "test:uri" ), path ) );

        File f = new File( temp.getRoot(), "cache/test:uri");
        f = new File( f, path );

        byte[] data = "This is a test with a bunch of data and some other stuff, in a big box sealed with chewing gum".getBytes();

        FileUtils.writeByteArrayToFile(f, data );

        logger.info( "Opening transfer input stream" );
        EventMetadata forceEventMetadata = new EventMetadata().set( FORCE_CHECKSUM, TRUE );
        try(InputStream stream = txfr.openInputStream( false, forceEventMetadata ))
        {
            logger.info( "Reading stream" );
            byte[] resultData = IOUtils.toByteArray( stream );

            logger.debug( "Result is {} bytes", resultData.length );

            assertThat( Arrays.equals( resultData, data ), equalTo( true ) );
        }

        final MessageDigest md = MessageDigest.getInstance( "MD5" );
        md.update( data );
        final byte[] digest = md.digest();
        final String digestHex = Hex.encodeHexString( digest );

        logger.debug( "Verifying .md5 file" );
        final Transfer md5Txfr = txfr.getSiblingMeta( ".md5" );
        String resultHex = null;
        try(InputStream in = md5Txfr.openInputStream())
        {
            resultHex = IOUtils.toString( in );
        }

        assertThat( resultHex, equalTo( digestHex ) );

        logger.debug( "Verifying MD5 in metadata consumer" );
        TransferMetadata metadata = metadataConsumer.getMetadata( txfr );
        assertThat( metadata, notNullValue() );

        Map<ContentDigest, String> digests = metadata.getDigests();
        assertThat( digests, CoreMatchers.<Map<ContentDigest, String>>notNullValue() );
        assertThat( digests.get( MD5 ), equalTo( digestHex ) );
    }

    @Test
    public void noChecksumOnReadWhenChecksumsAreDisabledForReads()
            throws Exception
    {
        String path = "my-path.txt";
        final Transfer txfr = fixture.getCache()
                                     .getTransfer(
                                             new ConcreteResource( new SimpleLocation( "test:uri" ), path ) );

        File f = new File( temp.getRoot(), "cache/test:uri");
        f = new File( f, path );

        byte[] data = "This is a test with a bunch of data and some other stuff, in a big box sealed with chewing gum".getBytes();

        FileUtils.writeByteArrayToFile(f, data );

        logger.info( "Opening transfer input stream" );
        EventMetadata forceEventMetadata = new EventMetadata();
        try(InputStream stream = txfr.openInputStream( false, forceEventMetadata ))
        {
            logger.info( "Reading stream" );
            byte[] resultData = IOUtils.toByteArray( stream );

            logger.debug( "Result is {} bytes", resultData.length );

            assertThat( Arrays.equals( resultData, data ), equalTo( true ) );
        }

        logger.debug( "Verifying .md5 file is missing" );
        final Transfer md5Txfr = txfr.getSiblingMeta( ".md5" );
        assertThat( md5Txfr.exists(), equalTo( false ) );

        logger.debug( "Verifying MD5 in metadata consumer is missing" );
        TransferMetadata metadata = metadataConsumer.getMetadata( txfr );
        assertThat( metadata, nullValue() );
    }

}
