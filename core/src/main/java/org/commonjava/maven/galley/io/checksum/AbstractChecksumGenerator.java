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

import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.metrics.TimingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

public abstract class AbstractChecksumGenerator
{
    protected static final String CHECKSUM_WRITE = "io.checksum.gen.write";

    protected static final String CHECKSUM_WRITE_OPEN = CHECKSUM_WRITE + ".open";

    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    private final MessageDigest digester;

    private final String checksumExtension;

    private final ContentDigest digestType;

    private final boolean writeChecksumFile;

    private final Function<String, TimingProvider> timerProviderFunction;

    private final Transfer checksumTransfer;

    private String digestHex;

    protected AbstractChecksumGenerator( final Transfer transfer, final String checksumExtension,
                                         final ContentDigest type, final boolean writeChecksumFile,
                                         final Function<String, TimingProvider> timerProviderFunction )
            throws IOException
    {
        this.checksumExtension = checksumExtension;
        digestType = type;
        this.writeChecksumFile = writeChecksumFile;
        this.timerProviderFunction = timerProviderFunction == null ? ( s)->null : timerProviderFunction;
        try
        {
            digester = MessageDigest.getInstance( digestType.digestName() );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IOException( "Cannot get MessageDigest for checksum type: '" + type + "': " + e.getMessage(), e );
        }

        if ( writeChecksumFile )
        {
            logger.debug( "Getting checksum transfer for: {}", transfer );
            this.checksumTransfer = getChecksumFile( transfer );
            logger.debug( "Locking checksum file: {}", checksumTransfer );
            this.checksumTransfer.lockWrite();
        }
        else
        {
            this.checksumTransfer = null;
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

    public final void update( final byte[] data, final int offset, final int len )
    {
        digester.update( data, offset, len );
    }

    public final void write()
            throws IOException
    {
        if ( !writeChecksumFile )
        {
            return;
        }

        TimingProvider writeTimer = timerProviderFunction.apply( CHECKSUM_WRITE );
        try
        {
            logger.info( "Writing {} file: {}", checksumExtension, checksumTransfer );

            TimingProvider openTimer = timerProviderFunction.apply( CHECKSUM_WRITE_OPEN );
            try (PrintStream out = new PrintStream( checksumTransfer.openOutputStream( TransferOperation.GENERATE ) ))
            {
                if ( openTimer != null )
                {
                    openTimer.stop();
                }

                out.print( getDigestHex() );
            }
        }
        finally
        {
            if ( writeTimer != null )
            {
                writeTimer.stop();
            }
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
