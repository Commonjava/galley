/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.filearc.internal;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.getArchiveFile;
import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.isJar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;

public class ZipDownload
    implements DownloadJob
{

    private TransferException error;

    private final Transfer txfr;

    public ZipDownload( final Transfer txfr )
    {
        this.txfr = txfr;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Transfer call()
    {
        final File src = getArchiveFile( txfr.getLocation()
                                             .getUri() );

        if ( !src.canRead() || src.isDirectory() )
        {
            return txfr;
        }

        final boolean isJar = isJar( txfr.getLocation()
                                         .getUri() );

        ZipFile zf = null;
        InputStream in = null;
        OutputStream out = null;
        try
        {
            zf = isJar ? new JarFile( src ) : new ZipFile( src );

            final ZipEntry entry = zf.getEntry( txfr.getPath() );
            if ( entry != null )
            {
                if ( entry.isDirectory() )
                {
                    error = new TransferException( "Cannot read stream. Source is a directory: %s!%s", txfr.getLocation()
                                                                                                           .getUri(), txfr.getPath() );
                }
                else
                {
                    in = zf.getInputStream( entry );
                    out = txfr.openOutputStream( TransferOperation.DOWNLOAD, false );

                    copy( in, out );

                    return txfr;
                }
            }
            else
            {
                error = new TransferException( "Cannot find entry: %s in: %s", txfr.getParent(), txfr.getLocation()
                                                                                                     .getUri() );
            }
        }
        catch ( final IOException e )
        {
            error = new TransferException( "Failed to copy from: %s to: %s. Reason: %s", e, src, txfr, e.getMessage() );
        }
        finally
        {
            closeQuietly( in );
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

            closeQuietly( out );
        }

        return null;
    }

}
