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
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.transport.ListingJob;

public class ZipListing
    implements ListingJob
{

    private TransferException error;

    private final Location location;

    private final String path;

    public ZipListing( final Location location, final String path )
    {
        this.location = location;
        this.path = path;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public ListingResult call()
    {
        final File src = getArchiveFile( location.getUri() );
        if ( !src.exists() )
        {
            return null;
        }

        final boolean isJar = isJar( location.getUri() );

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

            return new ListingResult( location, path, filenames.toArray( new String[filenames.size()] ) );
        }
        catch ( final IOException e )
        {
            error =
                new TransferException( "Failed to get listing for: %s from: %s to: %s. Reason: %s", e, path, src,
                                       e.getMessage() );
        }
        finally
        {
            closeQuietly( zf );
        }

        return null;
    }

}
