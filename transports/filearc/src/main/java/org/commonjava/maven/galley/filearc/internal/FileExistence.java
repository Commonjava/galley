package org.commonjava.maven.galley.filearc.internal;

import java.io.File;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;

public class FileExistence
    implements ExistenceJob
{

    private final File src;

    public FileExistence( final File src )
    {
        this.src = src;
    }

    @Override
    public TransferException getError()
    {
        return null;
    }

    @Override
    public Boolean call()
    {
        if ( src.exists() && src.canRead() )
        {
            return true;
        }

        return false;
    }

}
