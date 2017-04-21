/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.checksum.AbstractChecksumGenerator;
import org.commonjava.maven.galley.io.checksum.AbstractChecksumGeneratorFactory;
import org.commonjava.maven.galley.io.checksum.ChecksummingInputStream;
import org.commonjava.maven.galley.io.checksum.ChecksummingOutputStream;
import org.commonjava.maven.galley.io.checksum.TransferMetadataConsumer;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.commonjava.maven.galley.model.TransferOperation.DOWNLOAD;

@Alternative
@Named
public final class ChecksummingTransferDecorator
        extends AbstractTransferDecorator
{

    public static final String FORCE_CHECKSUM = "force-checksum";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final boolean checksumReaders;

    private final boolean checksumWriters;

    private final TransferMetadataConsumer consumer;

    private final Set<AbstractChecksumGeneratorFactory<?>> checksumFactories;

    private final Set<TransferOperation> writeChecksumFilesOn;

    private SpecialPathManager specialPathManager;

    public ChecksummingTransferDecorator( final Set<TransferOperation> writeChecksumFilesOn,
                                          final SpecialPathManager specialPathManager, final boolean checksumReaders,
                                          final boolean checksumWriters, final TransferMetadataConsumer consumer,
                                          final AbstractChecksumGeneratorFactory<?>... checksumFactories )
    {
        this.writeChecksumFilesOn = writeChecksumFilesOn;
        this.specialPathManager = specialPathManager;
        this.checksumReaders = checksumReaders;
        this.checksumWriters = checksumWriters;
        this.consumer = consumer;
        this.checksumFactories = new HashSet<AbstractChecksumGeneratorFactory<?>>( Arrays.asList( checksumFactories ) );
    }

    public ChecksummingTransferDecorator( final Set<TransferOperation> writeChecksumFilesOn,
                                          final SpecialPathManager specialPathManager, final boolean checksumReaders,
                                          final boolean checksumWriters, final TransferMetadataConsumer consumer,
                                          final Collection<AbstractChecksumGeneratorFactory<?>> checksumFactories )
    {
        this.writeChecksumFilesOn = writeChecksumFilesOn;
        this.specialPathManager = specialPathManager;
        this.checksumReaders = checksumReaders;
        this.checksumWriters = checksumWriters;
        this.consumer = consumer;
        if ( checksumFactories instanceof Set )
        {
            this.checksumFactories = (Set<AbstractChecksumGeneratorFactory<?>>) checksumFactories;
        }
        else
        {
            this.checksumFactories = new HashSet<AbstractChecksumGeneratorFactory<?>>( checksumFactories );
        }
    }

    public OutputStream decorateWrite( final OutputStream stream, final Transfer transfer, final TransferOperation op,
                                       final EventMetadata eventMetadata )
            throws IOException
    {
        boolean force = Boolean.TRUE.equals( eventMetadata.get( FORCE_CHECKSUM ) );
        if ( force || checksumWriters )
        {
            boolean writeChecksums = force || writeChecksumFilesOn == null || writeChecksumFilesOn.isEmpty()
                    || writeChecksumFilesOn.contains( op );

            SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer );

            if ( specialPathInfo == null || specialPathInfo.isDecoratable() )
            {
                logger.info( "Wrapping output stream to: {} for checksum generation.", transfer );
                return new ChecksummingOutputStream( checksumFactories, stream, transfer, consumer, writeChecksums );
            }
        }

        return stream;
    }

    public InputStream decorateRead( final InputStream stream, final Transfer transfer,
                                     final EventMetadata eventMetadata )
            throws IOException
    {
        boolean force = Boolean.TRUE.equals( eventMetadata.get( FORCE_CHECKSUM ) );
        if ( force || checksumReaders )
        {
            SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer );

            if ( specialPathInfo == null || specialPathInfo.isDecoratable() )
            {
                logger.info( "Wrapping input stream to: {} for checksum generation.", transfer );
                boolean writeChecksums = force || writeChecksumFilesOn == null || writeChecksumFilesOn.isEmpty()
                        || writeChecksumFilesOn.contains( DOWNLOAD );

                return new ChecksummingInputStream( checksumFactories, stream, transfer, consumer, writeChecksums );
            }
        }
        return stream;
    }

    public void decorateDelete( final Transfer transfer, final EventMetadata eventMetadata )
            throws IOException
    {
        if ( transfer.isDirectory() )
        {
            return;
        }

        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer );
        if ( specialPathInfo == null || specialPathInfo.isDeletable() )
        {
            for ( final AbstractChecksumGeneratorFactory<?> factory : checksumFactories )
            {
                final AbstractChecksumGenerator generator = factory.createGenerator( transfer );
                generator.delete();
            }
        }

        if ( consumer != null )
        {
            consumer.removeMetadata( transfer );
        }
    }
}
