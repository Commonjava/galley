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

import org.commonjava.maven.galley.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ChecksummingInputStream
        extends FilterInputStream
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Set<AbstractChecksumGenerator> checksums;

    private long size = 0;

    private final Transfer transfer;

    private final TransferMetadataConsumer metadataConsumer;

    private boolean writeChecksumFiles;

    public ChecksummingInputStream( final Set<AbstractChecksumGeneratorFactory<?>> checksumFactories,
                                    final InputStream stream, final Transfer transfer,
                                    final TransferMetadataConsumer metadataConsumer, final boolean writeChecksumFiles )
            throws IOException
    {
        super( stream );
        this.transfer = transfer;
        this.metadataConsumer = metadataConsumer;
        this.writeChecksumFiles = writeChecksumFiles;
        checksums = new HashSet<>();
        for ( final AbstractChecksumGeneratorFactory<?> factory : checksumFactories )
        {
            checksums.add( factory.createGenerator( transfer, writeChecksumFiles ) );
        }
    }

    @Override
    public void close()
            throws IOException
    {
        try
        {
            logger.trace( "START CLOSE: {}", transfer );
            logger.trace( "Read done: {} in: {}. Now, creating checksums.", transfer.getPath(), transfer.getLocation() );
            Map<ContentDigest, String> hexDigests = new HashMap<>();
            for ( final AbstractChecksumGenerator checksum : checksums )
            {
                hexDigests.put( checksum.getDigestType(), checksum.getDigestHex() );
                if ( writeChecksumFiles )
                {
                    logger.trace( "Writing checksum file for: {}", checksum.getDigestType() );
                    checksum.write();
                }
            }

            if ( metadataConsumer != null )
            {
                logger.trace( "Adding metadata for: {} to: {}", transfer, metadataConsumer );
                metadataConsumer.addMetadata( transfer, new TransferMetadata( hexDigests, size ) );
            }
            else
            {
                logger.trace( "No metadata consumer. NOT adding metadata." );
            }

        }
        finally
        {
            // NOTE: We close the main stream LAST, in case it's holding a file (or other) lock. This allows us to
            // finish out tasks BEFORE releasing that lock.
            super.close();
            logger.trace( "END CLOSE: {}", transfer );
        }
    }

    /**
     * Update the checksums we're tracking, and the overall file size. The pass through the data to the user.
     *
     * NOTE: It's not enough to override {@link FilterInputStream#read()}. This method is NOT called by
     * {@link FilterInputStream#read(byte[], int, int)}, so you MUST override both to get consistent behavior.

     * @throws IOException in case of nested read error
     */
    @Override
    public int read()
            throws IOException
    {
        logger.trace( "READ: {}", transfer );
        int data = super.read();
        logger.trace( "{} input", data );
        if ( data > -1 )
        {
            size++;
            logger.trace( "Updating with: {} (raw: {})", ( (byte) data & 0xff ), data );
            for ( final AbstractChecksumGenerator checksum : checksums )
            {
                checksum.update( (byte) data );
            }
        }
        else
        {
            logger.trace( "READ: <EOF>" );
        }

        return data;
    }

    /**
     * Update the checksums we're tracking, and the overall file size. The pass through the data to the user.
     *
     * NOTE: If you override {@link FilterInputStream#read(byte[])} as well as this method, and you call super.read(), you will
     * get DUPLICATE behavior (twice the effect) because this method DOES call {@link FilterInputStream#read(byte[], int, int)}.

     * @throws IOException in case of nested read error
     */
    @Override
    public int read( final byte[] b, final int off, final int len )
            throws IOException
    {
        int read = super.read( b, off, len );
        updateDigests( b, off, read );
        return read;
    }

    private void updateDigests( final byte[] b, final int off, final int read )
    {
        if ( read < 0 )
        {
            return;
        }

        size += read;
        logger.trace( "Updating with [buffer of size: {}]", read );
        for ( final AbstractChecksumGenerator checksum : checksums )
        {
            for ( int i = off; i < off + read; i++ )
            {
                checksum.update( (byte) ( b[i] & 0xff ) );
            }
        }
    }
}