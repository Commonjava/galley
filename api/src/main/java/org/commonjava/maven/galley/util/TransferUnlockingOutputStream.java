package org.commonjava.maven.galley.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.Transfer;

public class TransferUnlockingOutputStream
    extends FilterOutputStream
{

    private final Transfer transfer;

    public TransferUnlockingOutputStream( final OutputStream out, final Transfer transfer )
    {
        super( out );
        this.transfer = transfer;
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();
        transfer.unlock();
    }

}
