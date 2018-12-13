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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.ChecksummingTransferDecorator;
import org.commonjava.maven.galley.io.SpecialPathManagerImpl;
import org.commonjava.maven.galley.io.checksum.testutil.TestDecoratorAdvisor;
import org.commonjava.maven.galley.io.checksum.testutil.TestMetadataConsumer;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.testing.core.ApiFixture;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.commonjava.maven.galley.io.ChecksummingTransferDecorator.FORCE_CHECKSUM;
import static org.commonjava.maven.galley.io.checksum.ContentDigest.MD5;
import static org.commonjava.maven.galley.io.checksum.testutil.TestDecoratorAdvisor.DO_CHECKSUMS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Created by jdcasey on 4/27/17.
 */
public class ChecksummingTransferDecoratorTest
{
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    public ApiFixture fixture = new ApiFixture( temp );

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private TestMetadataConsumer metadataConsumer = new TestMetadataConsumer();

    @Test
    public void forceChecksumOnReadWhenChecksumsAreDisabledForReads()
            throws Exception
    {
        fixture.setDecorator( new ChecksummingTransferDecorator( Collections.<TransferOperation>emptySet(),
                                                                 new SpecialPathManagerImpl(), false, false,
                                                                 metadataConsumer, new Md5GeneratorFactory() ) );
        fixture.initMissingComponents();
        fixture.getCache().startReporting();

        String path = "my-path.txt";
        final Transfer txfr =
                fixture.getCache().getTransfer( new ConcreteResource( new SimpleLocation( "test:uri" ), path ) );

        File f = new File( temp.getRoot(), "cache/test:uri" );
        f = new File( f, path );

        byte[] data =
                "This is a test with a bunch of data and some other stuff, in a big box sealed with chewing gum".getBytes();

        FileUtils.writeByteArrayToFile( f, data );

        logger.info( "Opening transfer input stream" );
        EventMetadata forceEventMetadata = new EventMetadata().set( FORCE_CHECKSUM, TRUE );
        try (InputStream stream = txfr.openInputStream( false, forceEventMetadata ))
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
        fixture.setDecorator( new ChecksummingTransferDecorator( Collections.<TransferOperation>emptySet(),
                                                                 new SpecialPathManagerImpl(), false, false,
                                                                 metadataConsumer, new Md5GeneratorFactory() ) );
        fixture.initMissingComponents();
        fixture.getCache().startReporting();

        String path = "my-path.txt";
        final Transfer txfr =
                fixture.getCache().getTransfer( new ConcreteResource( new SimpleLocation( "test:uri" ), path ) );

        File f = new File( temp.getRoot(), "cache/test:uri" );
        f = new File( f, path );

        byte[] data =
                "This is a test with a bunch of data and some other stuff, in a big box sealed with chewing gum".getBytes();

        FileUtils.writeByteArrayToFile( f, data );

        logger.info( "Opening transfer input stream" );
        EventMetadata forceEventMetadata = new EventMetadata();
        try (InputStream stream = txfr.openInputStream( false, forceEventMetadata ))
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

    @Test
    public void customChecksumReaderFilter()
            throws Exception
    {
        String path = "my-path.txt";

        fixture.setDecorator(
                new ChecksummingTransferDecorator( new TestDecoratorAdvisor(), new DisabledChecksummingDecoratorAdvisor(),
                                                   new SpecialPathManagerImpl(), metadataConsumer,
                                                   new Md5GeneratorFactory() ) );
        fixture.initMissingComponents();
        fixture.getCache().startReporting();

        final Transfer txfr =
                fixture.getCache().getTransfer( new ConcreteResource( new SimpleLocation( "test:uri" ), path ) );

        File f = new File( temp.getRoot(), "cache/test:uri" );
        f = new File( f, path );

        byte[] data =
                "This is a test with a bunch of data and some other stuff, in a big box sealed with chewing gum".getBytes();

        FileUtils.writeByteArrayToFile( f, data );

        EventMetadata em = new EventMetadata();

        logger.debug( "Reading stream with EventMetadata advice: {}", em.get( DO_CHECKSUMS ) );
        assertRead( txfr, data, em, false, false );

        em = new EventMetadata().set( DO_CHECKSUMS,
                                      ChecksummingDecoratorAdvisor.ChecksumAdvice.CALCULATE_NO_WRITE );

        logger.debug( "Reading stream with EventMetadata advice: {}", em.get( DO_CHECKSUMS ) );
        assertRead( txfr, data, em, false, true );

        logger.debug( "Removing checksum metadata from consumer" );
        metadataConsumer.removeMetadata( txfr );

        em = new EventMetadata().set( DO_CHECKSUMS,
                                      ChecksummingDecoratorAdvisor.ChecksumAdvice.CALCULATE_AND_WRITE );

        logger.debug( "Reading stream with EventMetadata advice: {}", em.get( DO_CHECKSUMS ) );
        assertRead( txfr, data, em, true, true );

    }

    private void assertRead( final Transfer txfr, final byte[] data, final EventMetadata em,
                             final boolean checksumFileExists, final boolean metadataConsumerContains )
            throws IOException
    {
        try (InputStream stream = txfr.openInputStream( false, em ))
        {
            logger.info( "Reading stream" );
            byte[] resultData = IOUtils.toByteArray( stream );

            logger.debug( "Result is {} bytes", resultData.length );

            assertThat( Arrays.equals( resultData, data ), equalTo( true ) );
        }

        logger.debug( "Verifying .md5 file is {}", checksumFileExists ? "available" : "misssing" );
        final Transfer md5Txfr = txfr.getSiblingMeta( ".md5" );
        assertThat( md5Txfr.exists(), equalTo( checksumFileExists ) );

        TransferMetadata metadata = metadataConsumer.getMetadata( txfr );
        if ( metadataConsumerContains )
        {
            logger.debug( "Verifying MD5 in metadata consumer is available" );
            assertThat( metadata, notNullValue() );

            String actualMd5 = metadata.getDigests().get( MD5 );
            String expectedMd5 = md5Hex( data );
            logger.debug( "Verifying actual MD5 content: '{}' vs. expected: '{}'", actualMd5, expectedMd5 );

            assertThat( actualMd5, equalTo( expectedMd5 ) );
        }
        else
        {
            logger.debug( "Verifying MD5 in metadata consumer is missing" );
            assertThat( metadata, nullValue() );
        }
    }

}
