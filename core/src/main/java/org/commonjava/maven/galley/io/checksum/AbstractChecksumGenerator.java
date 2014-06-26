package org.commonjava.maven.galley.io.checksum;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.commonjava.maven.galley.model.Transfer;
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
        final File checksumFile = getChecksumFile( transfer );
        logger.info( "Writing {} file: {}", checksumExtension, checksumFile );

        final File dir = checksumFile.getParentFile();
        if ( dir != null && !dir.exists() && !dir.mkdirs() )
        {
            throw new IOException( "Cannot create directory: " + dir );
        }

        FileUtils.write( checksumFile, encodeHexString( digester.digest() ) );
    }

    public final void delete()
        throws IOException
    {
        final File checksumFile = getChecksumFile( transfer );
        if ( checksumFile.exists() )
        {
            FileUtils.forceDelete( checksumFile );
        }
    }

    private final File getChecksumFile( final Transfer transfer )
    {
        final File f = transfer.getDetachedFile();
        return new File( f.getParentFile(), f.getName() + checksumExtension );
    }

}
