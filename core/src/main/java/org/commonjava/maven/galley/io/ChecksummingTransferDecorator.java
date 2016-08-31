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
package org.commonjava.maven.galley.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.galley.io.checksum.AbstractChecksumGenerator;
import org.commonjava.maven.galley.io.checksum.AbstractChecksumGeneratorFactory;
import org.commonjava.maven.galley.io.checksum.ChecksummingOutputStream;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

@Alternative
@Named
public final class ChecksummingTransferDecorator
    extends AbstractTransferDecorator
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Set<AbstractChecksumGeneratorFactory<?>> checksumFactories;

    private final Set<TransferOperation> ops;

    private SpecialPathManager specialPathManager;


    public ChecksummingTransferDecorator( final Set<TransferOperation> ops, final SpecialPathManager specialPathManager,
                                          final AbstractChecksumGeneratorFactory<?>... checksumFactories )
    {
        this( null, ops, specialPathManager, checksumFactories );
    }

    public ChecksummingTransferDecorator( final TransferDecorator next, final Set<TransferOperation> ops,
                                          final SpecialPathManager specialPathManager,
                                          final AbstractChecksumGeneratorFactory<?>... checksumFactories )
    {
        super( next );
        this.ops = ops;
        this.specialPathManager = specialPathManager;
        this.checksumFactories = new HashSet<AbstractChecksumGeneratorFactory<?>>( Arrays.asList( checksumFactories ) );
    }

    public ChecksummingTransferDecorator( final Set<TransferOperation> ops, final SpecialPathManager specialPathManager,
                                          final Collection<AbstractChecksumGeneratorFactory<?>> checksumFactories )
    {
        this( null, ops, specialPathManager, checksumFactories );
    }

    public ChecksummingTransferDecorator( final TransferDecorator next, final Set<TransferOperation> ops,
                                          final SpecialPathManager specialPathManager,
                                          final Collection<AbstractChecksumGeneratorFactory<?>> checksumFactories )
    {
        super( next );
        this.ops = ops;
        this.specialPathManager = specialPathManager;
        if ( checksumFactories instanceof Set )
        {
            this.checksumFactories = (Set<AbstractChecksumGeneratorFactory<?>>) checksumFactories;
        }
        else
        {
            this.checksumFactories = new HashSet<AbstractChecksumGeneratorFactory<?>>( checksumFactories );
        }
    }

    @Override
    protected OutputStream decorateWriteInternal( final OutputStream stream, final Transfer transfer,
                                                  final TransferOperation op )
        throws IOException
    {
        if ( ops.contains( op ) )
        {
            SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer );

            if ( specialPathInfo == null || specialPathInfo.isDecoratable() )
            {
                logger.info( "Wrapping output stream to: {} for checksum generation.", transfer );
                return new ChecksummingOutputStream( checksumFactories, stream, transfer );
            }
        }

        return stream;
    }

    @Override
    protected InputStream decorateReadInternal( final InputStream stream, final Transfer transfer )
        throws IOException
    {
        return stream;
    }

    @Override
    protected void decorateDeleteInternal( final Transfer transfer )
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
    }
}
