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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.galley.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChecksummingOutputStream
    extends FilterOutputStream
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Set<AbstractChecksumGenerator> checksums;

    private final Transfer transfer;

    public ChecksummingOutputStream( final Set<AbstractChecksumGeneratorFactory<?>> checksumFactories,
                                     final OutputStream stream, final Transfer transfer )
        throws IOException
    {
        super( stream );
        this.transfer = transfer;
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

        logger.debug( "Wrote: {} in: {}. Now, writing checksums.", transfer.getPath(), transfer.getLocation() );
        for ( final AbstractChecksumGenerator checksum : checksums )
        {
            checksum.write();
        }
    }

    @Override
    public void write( final int data )
        throws IOException
    {
        super.write( data );
        for ( final AbstractChecksumGenerator checksum : checksums )
        {
            checksum.update( (byte) data );
        }
    }

}