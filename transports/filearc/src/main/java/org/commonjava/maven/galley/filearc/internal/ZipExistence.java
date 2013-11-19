package org.commonjava.maven.galley.filearc.internal;

import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.getArchiveFile;
import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.isJar;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;

public class ZipExistence
    implements ExistenceJob
{

    private TransferException error;

    private final ConcreteResource resource;

    public ZipExistence( final ConcreteResource resource )
    {
        this.resource = resource;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Boolean call()
    {
        final File src = getArchiveFile( resource.getLocationUri() );
        if ( !src.canRead() || src.isDirectory() )
        {
            return null;
        }

        final boolean isJar = isJar( resource.getLocationUri() );

        ZipFile zf = null;
        try
        {
            if ( isJar )
            {
                zf = new JarFile( src );
            }
            else
            {
                zf = new ZipFile( src );
            }

            final String path = resource.getPath();

            boolean found = false;
            for ( final ZipEntry entry : Collections.list( zf.entries() ) )
            {
                final String name = entry.getName();
                if ( name.equals( path ) )
                {
                    found = true;
                    break;
                }
            }

            return found;
        }
        catch ( final IOException e )
        {
            error = new TransferException( "Failed to get listing for: %s to: %s. Reason: %s", e, resource, e.getMessage() );
        }
        finally
        {
            if ( zf != null )
            {
                try
                {
                    zf.close();
                }
                catch ( final IOException e )
                {
                }
            }
        }

        return false;
    }

}
