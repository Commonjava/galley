package org.commonjava.maven.galley.testutil;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

public class TestDownload
    implements TestDownloadJob
{

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
            stream = transfer.openOutputStream( TransferOperation.DOWNLOAD );
            IOUtils.write( data, stream );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }

        return transfer;
    }

    @Override
    public void setTransfer( final Transfer transfer )
    {
        this.transfer = transfer;
    }

}
