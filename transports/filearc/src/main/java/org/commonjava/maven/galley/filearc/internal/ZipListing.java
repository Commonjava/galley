package org.commonjava.maven.galley.filearc.internal;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.getArchiveFile;
import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.isJar;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.spi.transport.ListingJob;

public class ZipListing
    implements ListingJob
{

    private TransferException error;

    private final Resource resource;

    public ZipListing( final Resource resource )
    {
        this.resource = resource;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public ListingResult call()
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
            final int pathLen = path.length();
            final TreeSet<String> filenames = new TreeSet<>();
            for ( final ZipEntry entry : Collections.list( zf.entries() ) )
            {
                String name = entry.getName();
                if ( name.startsWith( path ) )
                {
                    name = name.substring( pathLen );

                    if ( name.startsWith( "/" ) && name.length() > 1 )
                    {
                        name = name.substring( 1 );

                        if ( name.indexOf( "/" ) < 0 )
                        {
                            filenames.add( name );
                        }
                    }
                }
            }

            return new ListingResult( resource, filenames.toArray( new String[filenames.size()] ) );
        }
        catch ( final IOException e )
        {
            error = new TransferException( "Failed to get listing for: %s to: %s. Reason: %s", e, resource, e.getMessage() );
        }
        finally
        {
            closeQuietly( zf );
        }

        return null;
    }

}
