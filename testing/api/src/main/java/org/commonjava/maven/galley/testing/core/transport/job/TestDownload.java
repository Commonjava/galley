package org.commonjava.maven.galley.testing.core.transport.job;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.util.logging.Logger;

public class TestDownload
    implements DownloadJob
{

    private final Logger logger = new Logger( getClass() );

    private final TransferException error;

    private final byte[] data;

    private Transfer transfer;

    public TestDownload( final TransferException error )
    {
        this.data = null;
        this.error = error;
    }

    public TestDownload( final byte[] data )
    {
        this.data = data;
        this.error = null;
    }

    public TestDownload( final String classpathResource )
        throws IOException
    {
        final InputStream stream = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream( classpathResource );
        if ( stream == null )
        {
            throw new IllegalArgumentException( "classpath resource: " + classpathResource + " is missing." );
        }

        this.data = IOUtils.toByteArray( stream );
        this.error = null;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Transfer call()
        throws Exception
    {
        if ( data == null )
        {
            return null;
        }

        OutputStream stream = null;
        try
        {
            logger.info( "Writing '%s' to: %s.", new String( data ), transfer.getDetachedFile() );
            stream = transfer.openOutputStream( TransferOperation.DOWNLOAD );
            IOUtils.write( data, stream );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }

        return transfer;
    }

    public void setTransfer( final Transfer transfer )
    {
        this.transfer = transfer;
    }

}
