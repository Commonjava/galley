/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.filearc.internal;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

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
    extends AbstractZipOperation
    implements PublishJob
{

    private final InputStream stream;

    private TransferException error;

    private boolean success;

    public ZipPublish( final ConcreteResource resource, final InputStream stream )
    {
        super( resource );
        this.stream = stream;
    }

    @Override
    public long getTransferSize()
    {
        return -1;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public ZipPublish call()
    {
        final String path = getFullPath();
        final File dest = getZipFile();

        if ( dest.exists() )
        {
            success = rewriteArchive( dest, path );
        }
        else
        {
            success = writeArchive( dest, path );
        }

        return this;
    }

    @Override
    public boolean isSuccessful()
    {
        return success;
    }

    private Boolean writeArchive( final File dest, final String path )
    {
        final boolean isJar = isJarOperation();
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
            closeQuietly( stream );
        }

        return false;
    }

    private Boolean rewriteArchive( final File dest, final String path )
    {
        final boolean isJar = isJarOperation();
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
            if ( zfOut != null )
            {
                try
                {
                    zfOut.close();
                }
                catch ( final IOException e )
                {
                }
            }

            if ( zfIn != null )
            {
                try
                {
                    zfIn.close();
                }
                catch ( final IOException e )
                {
                }
            }

            closeQuietly( stream );
        }

        return false;
    }

}
