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
import java.io.OutputStream;
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

    private final String checksumExtension;

    private ContentDigest digestType;

    private final Transfer checksumTransfer;

    private String digestHex;

    protected AbstractChecksumGenerator( final Transfer transfer, final String checksumExtension, final ContentDigest type )
        throws IOException
    {
        this.checksumExtension = checksumExtension;
        digestType = type;
        try
        {
            digester = MessageDigest.getInstance( digestType.digestName() );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IOException( "Cannot get MessageDigest for checksum type: '" + type + "': " + e.getMessage(), e );
        }

        logger.debug( "Getting checksum transfer for: {}", transfer );
        this.checksumTransfer = getChecksumFile( transfer );
        logger.debug( "Locking checksum file: {}", checksumTransfer );
        this.checksumTransfer.lockWrite();
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
        logger.info( "Writing {} file: {}", checksumExtension, checksumTransfer );

        PrintStream out = null;
        OutputStream stream = null;
        try
        {
            stream = checksumTransfer.openOutputStream( TransferOperation.GENERATE );
            out = new PrintStream( stream );
            out.print( getDigestHex() );
        }
        finally
        {
            IOUtils.closeQuietly( out );
            IOUtils.closeQuietly( stream );
        }
    }

    public synchronized String getDigestHex()
    {
        if ( digestHex == null )
        {
            digestHex = encodeHexString( digester.digest() );
        }
        return digestHex;
    }

    public final void delete()
        throws IOException
    {
        if ( checksumTransfer.exists() )
        {
            checksumTransfer.delete();
            checksumTransfer.unlock();
        }
    }

    private Transfer getChecksumFile( final Transfer transfer )
    {
        return transfer.getSiblingMeta( checksumExtension );
    }

    public ContentDigest getDigestType()
    {
        return digestType;
    }
}
