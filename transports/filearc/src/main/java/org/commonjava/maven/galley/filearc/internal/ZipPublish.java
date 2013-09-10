package org.commonjava.maven.galley.filearc.internal;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.isJar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.transport.PublishJob;

public class ZipPublish
    implements PublishJob
{

    private final InputStream stream;

    private TransferException error;

    private final ConcreteResource resource;

    public ZipPublish( final ConcreteResource resource, final InputStream stream )
    {
        this.resource = resource;
        this.stream = stream;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Boolean call()
    {
        final String path = resource.getPath();
        final File dest = new File( resource.getLocationUri() );

        if ( dest.exists() )
        {
            return rewriteArchive( dest, path );
        }
        else
        {
            return writeArchive( dest, path );
        }
    }

    @SuppressWarnings( "resource" )
    private Boolean writeArchive( final File dest, final String path )
    {
        final boolean isJar = isJar( dest.getPath() );
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        ZipFile zf = null;
        try
        {
            fos = new FileOutputStream( dest );
            zos = isJar ? new JarOutputStream( fos ) : new ZipOutputStream( fos );

            zf = isJar ? new JarFile( dest ) : new ZipFile( dest );

            final ZipEntry entry = zf.getEntry( path );
            zos.putNextEntry( entry );
            copy( stream, zos );

            return true;
        }
        catch ( final IOException e )
        {
            error = new TransferException( "Failed to write path: %s to NEW archive: %s. Reason: %s", e, path, dest, e.getMessage() );
        }
        finally
        {
            closeQuietly( zos );
            closeQuietly( fos );
            closeQuietly( zf );
            closeQuietly( stream );
        }

        return false;
    }

    private Boolean rewriteArchive( final File dest, final String path )
    {
        final boolean isJar = isJar( dest.getPath() );
        final File src = new File( dest.getParentFile(), dest.getName() + ".old" );
        dest.renameTo( src );

        InputStream zin = null;
        ZipFile zfIn = null;

        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        ZipFile zfOut = null;
        try
        {
            fos = new FileOutputStream( dest );
            zos = isJar ? new JarOutputStream( fos ) : new ZipOutputStream( fos );

            zfOut = isJar ? new JarFile( dest ) : new ZipFile( dest );
            zfIn = isJar ? new JarFile( src ) : new ZipFile( src );

            for ( final Enumeration<? extends ZipEntry> en = zfIn.entries(); en.hasMoreElements(); )
            {
                final ZipEntry inEntry = en.nextElement();
                final String inPath = inEntry.getName();
                try
                {
                    if ( inPath.equals( path ) )
                    {
                        zin = stream;
                    }
                    else
                    {
                        zin = zfIn.getInputStream( inEntry );
                    }

                    final ZipEntry entry = zfOut.getEntry( inPath );
                    zos.putNextEntry( entry );
                    copy( stream, zos );
                }
                finally
                {
                    closeQuietly( zin );
                }
            }

            return true;
        }
        catch ( final IOException e )
        {
            error = new TransferException( "Failed to write path: %s to EXISTING archive: %s. Reason: %s", e, path, dest, e.getMessage() );
        }
        finally
        {
            closeQuietly( zos );
            closeQuietly( fos );
            closeQuietly( zfOut );
            closeQuietly( zfIn );
            closeQuietly( stream );
        }

        return false;
    }

}
