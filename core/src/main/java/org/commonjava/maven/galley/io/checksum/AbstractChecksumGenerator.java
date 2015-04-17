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

import static org.apache.commons.codec.binary.Hex.encodeHexString;

import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractChecksumGenerator
{

    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    private final MessageDigest digester;

    private final Transfer transfer;

    private final String checksumExtension;

    protected AbstractChecksumGenerator( final Transfer transfer, final String checksumExtension, final String type )
        throws IOException
    {
        this.transfer = transfer;
        this.checksumExtension = checksumExtension;
        try
        {
            digester = MessageDigest.getInstance( type );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IOException( "Cannot get MessageDigest for checksum type: '" + type + "': " + e.getMessage(), e );
        }
    }

    public final void update( final byte[] data )
    {
        digester.update( data );
    }

    public final void update( final byte data )
    {
        digester.update( data );
    }

    public final void write()
        throws IOException
    {
        final Transfer checksumFile = getChecksumFile( transfer );
        logger.info( "Writing {} file: {}", checksumExtension, checksumFile );

        PrintStream out = null;
        try
        {
            out = new PrintStream( checksumFile.openOutputStream( TransferOperation.GENERATE ) );
            out.print( encodeHexString( digester.digest() ) );
        }
        finally
        {
            IOUtils.closeQuietly( out );
        }
    }

    public final void delete()
        throws IOException
    {
        final Transfer checksumFile = getChecksumFile( transfer );
        if ( checksumFile.exists() )
        {
            checksumFile.delete();
        }
    }

    private final Transfer getChecksumFile( final Transfer transfer )
    {
        return transfer.getSiblingMeta( checksumExtension );
    }

}
