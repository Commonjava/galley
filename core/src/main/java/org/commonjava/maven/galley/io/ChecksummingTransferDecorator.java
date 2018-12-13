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
package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.checksum.AbstractChecksumGenerator;
import org.commonjava.maven.galley.io.checksum.AbstractChecksumGeneratorFactory;
import org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor;
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

import static org.commonjava.maven.galley.io.DeprecatedChecksummingFilter.calculateWriteOperations;
import static org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor.ChecksumAdvice.CALCULATE_AND_WRITE;
import static org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor.ChecksumAdvice.CALCULATE_NO_WRITE;
import static org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor.ChecksumAdvice.NO_DECORATE;
import static org.commonjava.maven.galley.model.TransferOperation.DOWNLOAD;

@Alternative
@Named
public final class ChecksummingTransferDecorator
        extends AbstractTransferDecorator
{

    public static final String FORCE_CHECKSUM = "force-checksum";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final ChecksummingDecoratorAdvisor writerFilter;

    private final ChecksummingDecoratorAdvisor readerFilter;

    private final TransferMetadataConsumer consumer;

    private final Set<AbstractChecksumGeneratorFactory<?>> checksumFactories;

    private SpecialPathManager specialPathManager;

    public ChecksummingTransferDecorator( final ChecksummingDecoratorAdvisor readerFilter,
                                          final ChecksummingDecoratorAdvisor writerFilter,
                                          SpecialPathManager specialPathManager, TransferMetadataConsumer consumer,
                                          Set<AbstractChecksumGeneratorFactory<?>> checksumFactories )
    {
        this.readerFilter = readerFilter;
        this.writerFilter = writerFilter;
        this.specialPathManager = specialPathManager;
        this.consumer = consumer;
        this.checksumFactories = checksumFactories;
    }

    public ChecksummingTransferDecorator( final ChecksummingDecoratorAdvisor readerFilter,
                                          final ChecksummingDecoratorAdvisor writerFilter,
                                          SpecialPathManager specialPathManager, TransferMetadataConsumer consumer,
                                          AbstractChecksumGeneratorFactory<?>... checksumFactories )
    {
        this( readerFilter, writerFilter, specialPathManager, consumer,
              new HashSet<>( Arrays.asList( checksumFactories ) ) );
    }

    /**
     * @see #ChecksummingTransferDecorator(ChecksummingDecoratorAdvisor, ChecksummingDecoratorAdvisor, SpecialPathManager, TransferMetadataConsumer, Set)
     */
    @Deprecated
    public ChecksummingTransferDecorator( final Set<TransferOperation> writeChecksumFilesOn,
                                          final SpecialPathManager specialPathManager, final boolean checksumReaders,
                                          final boolean checksumWriters, final TransferMetadataConsumer consumer,
                                          final AbstractChecksumGeneratorFactory<?>... checksumFactories )
    {
        this( new DeprecatedChecksummingFilter( checksumReaders,
                                                calculateWriteOperations( writeChecksumFilesOn, DOWNLOAD ) ),
              new DeprecatedChecksummingFilter( checksumWriters, writeChecksumFilesOn ), specialPathManager, consumer,
              new HashSet<>( Arrays.asList( checksumFactories ) ) );
    }

    /**
     * @see #ChecksummingTransferDecorator(ChecksummingDecoratorAdvisor, ChecksummingDecoratorAdvisor, SpecialPathManager, TransferMetadataConsumer, Set)
     */
    @Deprecated
    public ChecksummingTransferDecorator( final Set<TransferOperation> writeChecksumFilesOn,
                                          final SpecialPathManager specialPathManager, final boolean checksumReaders,
                                          final boolean checksumWriters, final TransferMetadataConsumer consumer,
                                          final Collection<AbstractChecksumGeneratorFactory<?>> checksumFactories )
    {
        this( new DeprecatedChecksummingFilter( checksumReaders,
                                                calculateWriteOperations( writeChecksumFilesOn, DOWNLOAD ) ),
              new DeprecatedChecksummingFilter( checksumWriters, writeChecksumFilesOn ), specialPathManager, consumer,
              toSet( checksumFactories ) );
    }

    private static Set<AbstractChecksumGeneratorFactory<?>> toSet(
            final Collection<AbstractChecksumGeneratorFactory<?>> checksumFactories )
    {
        Set<AbstractChecksumGeneratorFactory<?>> result;
        if ( checksumFactories instanceof Set )
        {
            result = (Set<AbstractChecksumGeneratorFactory<?>>) checksumFactories;
        }
        else
        {
            result = new HashSet<>( checksumFactories );
        }

        return result;
    }

    public OutputStream decorateWrite( final OutputStream stream, final Transfer transfer, final TransferOperation op,
                                       final EventMetadata eventMetadata )
            throws IOException
    {
        Object forceObj = eventMetadata.get( FORCE_CHECKSUM );
        boolean force = Boolean.TRUE.equals( forceObj ) || Boolean.parseBoolean( String.valueOf( forceObj ) );

        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer, eventMetadata.getPackageType() );

        logger.trace( "SpecialPathInfo for: {} is: {} (decoratable? {})", transfer, specialPathInfo,
                      ( specialPathInfo == null || specialPathInfo.isDecoratable() ) );

        if ( force || specialPathInfo == null || specialPathInfo.isDecoratable() )
        {
            ChecksummingDecoratorAdvisor.ChecksumAdvice advice =
                    writerFilter.getDecorationAdvice( transfer, op, eventMetadata );

            if ( force && advice == NO_DECORATE )
            {
                advice = CALCULATE_NO_WRITE;
            }

            boolean consumerNeedsIt = ( consumer == null || consumer.needsMetadataFor( transfer ) );
            logger.trace( "Advice is: {} for {} of: {} (and consumer is missing or needs it? {})", advice, op, transfer, consumerNeedsIt );

            // Cases when we want to do checksumming:
            // 0. if we're forcing recalculation
            // 1. if we need to write checksum files for this
            // 2. if we have a metadata consumer AND the consumer needs metadata for this transfer
            if ( advice != NO_DECORATE && ( consumer == null || consumer.needsMetadataFor( transfer ) ) )
            {
                logger.trace( "Wrapping output stream to: {} for checksum generation.", transfer );
                return new ChecksummingOutputStream( checksumFactories, stream, transfer, consumer,
                                                     advice == CALCULATE_AND_WRITE );
            }
        }

        logger.trace( "NOT decorating write with ChecksummingTransferDecorator for: {}", transfer );
        return stream;
    }

    @Override
    public InputStream decorateRead( final InputStream stream, final Transfer transfer,
                                     final EventMetadata eventMetadata )
            throws IOException
    {
        Object forceObj = eventMetadata.get( FORCE_CHECKSUM );
        boolean force = Boolean.TRUE.equals( forceObj ) || Boolean.parseBoolean( String.valueOf( forceObj ) );

        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer, eventMetadata.getPackageType() );

        logger.trace( "SpecialPathInfo for: {} is: {} (decoratable? {})", transfer, specialPathInfo,
                      ( specialPathInfo == null || specialPathInfo.isDecoratable() ) );

        if ( force || specialPathInfo == null || specialPathInfo.isDecoratable() )
        {
            ChecksummingDecoratorAdvisor.ChecksumAdvice advice =
                    readerFilter.getDecorationAdvice( transfer, DOWNLOAD, eventMetadata );

            if ( force && advice == NO_DECORATE )
            {
                advice = CALCULATE_NO_WRITE;
            }

            boolean consumerNeedsIt = ( consumer == null || consumer.needsMetadataFor( transfer ) );
            logger.trace( "Advice is: {} for {} of: {} (and consumer is missing or needs it? {})", advice, DOWNLOAD, transfer, consumerNeedsIt );

            // Cases when we want to do checksumming:
            // 0. if we're forcing recalculation
            // 1. if we need to write checksum files for this
            // 2. if we have a metadata consumer AND the consumer needs metadata for this transfer
            if ( advice != NO_DECORATE && ( consumer == null || consumer.needsMetadataFor( transfer ) ) )
            {
                return new ChecksummingInputStream( checksumFactories, stream, transfer, consumer,
                                                    advice == CALCULATE_AND_WRITE );
            }
        }

        logger.trace( "NOT decorating read with ChecksummingTransferDecorator for: {}", transfer );
        return stream;
    }

    public void decorateDelete( final Transfer transfer, final EventMetadata eventMetadata )
            throws IOException
    {
        if ( transfer.isDirectory() )
        {
            return;
        }

        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer, eventMetadata.getPackageType() );
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
