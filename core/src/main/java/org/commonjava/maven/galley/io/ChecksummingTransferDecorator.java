/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

public final class ChecksummingTransferDecorator
    extends AbstractTransferDecorator
{

    private final Set<AbstractChecksumGeneratorFactory<?>> checksumFactories;

    private final Set<TransferOperation> ops;

    private final Set<String> ignoredFileEndings;

    public ChecksummingTransferDecorator( final Set<TransferOperation> ops, final Set<String> ignoredFileEndings,
                                          final AbstractChecksumGeneratorFactory<?>... checksumFactories )
    {
        this.ops = ops;
        this.ignoredFileEndings = ignoredFileEndings;
        this.checksumFactories = new HashSet<AbstractChecksumGeneratorFactory<?>>( Arrays.asList( checksumFactories ) );
    }

    public ChecksummingTransferDecorator( final Set<TransferOperation> ops, final Set<String> ignoredFileEndings,
                                          final Collection<AbstractChecksumGeneratorFactory<?>> checksumFactories )
    {
        this.ops = ops;
        this.ignoredFileEndings = ignoredFileEndings;
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
    public OutputStream decorateWrite( final OutputStream stream, final Transfer transfer, final TransferOperation op )
        throws IOException
    {
        if ( ops.contains( op ) )
        {
            if ( ignoredFileEndings == null || ignoredFileEndings.isEmpty() )
            {
                return new ChecksummingOutputStream( checksumFactories, stream, transfer );
            }
            else
            {
                final String path = transfer.getPath();
                boolean ignored = false;
                for ( final String ending : ignoredFileEndings )
                {
                    if ( path.endsWith( ending ) )
                    {
                        ignored = true;
                        break;
                    }
                }

                if ( !ignored )
                {
                    return new ChecksummingOutputStream( checksumFactories, stream, transfer );
                }
            }
        }

        return stream;
    }

    @Override
    public InputStream decorateRead( final InputStream stream, final Transfer transfer )
        throws IOException
    {
        return stream;
    }

    @Override
    public void decorateDelete( final Transfer transfer )
        throws IOException
    {
        boolean delete = false;
        if ( ignoredFileEndings == null || ignoredFileEndings.isEmpty() )
        {
            delete = true;
        }
        else
        {
            final String path = transfer.getPath();
            boolean ignored = false;
            for ( final String ending : ignoredFileEndings )
            {
                if ( path.endsWith( ending ) )
                {
                    ignored = true;
                    break;
                }
            }

            if ( !ignored )
            {
                delete = true;
            }
        }

        if ( delete )
        {
            for ( final AbstractChecksumGeneratorFactory<?> factory : checksumFactories )
            {
                final AbstractChecksumGenerator generator = factory.createGenerator( transfer );
                generator.delete();
            }
        }
    }
}
