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
