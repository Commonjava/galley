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
                                    final InputStream stream, final Transfer transfer )
            throws IOException
    {
        this( checksumFactories, stream, transfer, null, true );
    }

    public ChecksummingInputStream( final Set<AbstractChecksumGeneratorFactory<?>> checksumFactories,
                                    final InputStream stream, final Transfer transfer,
                                    final TransferMetadataConsumer metadataConsumer, final boolean writeChecksumFiles )
        throws IOException
    {
        super( stream );
        this.transfer = transfer;
        this.metadataConsumer = metadataConsumer;
        this.writeChecksumFiles = writeChecksumFiles;
        checksums = new HashSet<AbstractChecksumGenerator>();
        for ( final AbstractChecksumGeneratorFactory<?> factory : checksumFactories )
        {
            checksums.add( factory.createGenerator( transfer ) );
        }
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();

        logger.debug( "Read: {} in: {}. Now, creating checksums.", transfer.getPath(), transfer.getLocation() );
        Map<ContentDigest, String> hexDigests = new HashMap<>();
        for ( final AbstractChecksumGenerator checksum : checksums )
        {
            hexDigests.put( checksum.getDigestType(), checksum.getDigestHex() );
            if ( writeChecksumFiles )
            {
                checksum.write();
            }
        }

        if ( metadataConsumer != null )
        {
            metadataConsumer.addMetadata( transfer, new TransferMetadata( hexDigests, size ) );
        }
    }

    @Override
    public int read()
        throws IOException
    {
        int data = super.read();
        if ( data != -1 )
        {
            size++;
            for ( final AbstractChecksumGenerator checksum : checksums )
            {
                checksum.update( (byte) data );
            }
        }

        return data;
    }

}