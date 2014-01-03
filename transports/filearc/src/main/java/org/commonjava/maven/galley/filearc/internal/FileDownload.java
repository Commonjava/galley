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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;

public class FileDownload
    implements DownloadJob
{

    private TransferException error;

    private final Transfer txfr;

    private final File src;

    public FileDownload( final Transfer txfr, final File src )
    {
        this.txfr = txfr;
        this.src = src;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Transfer call()
    {
        FileInputStream in = null;
        OutputStream out = null;
        try
        {
            if ( src.exists() && !src.isDirectory() )
            {
                in = new FileInputStream( src );
                out = txfr.openOutputStream( TransferOperation.DOWNLOAD, false );
                copy( in, out );
            }

            return txfr;
        }
        catch ( final IOException e )
        {
            error = new TransferException( "Failed to copy from: %s to: %s. Reason: %s", e, src, txfr, e.getMessage() );
        }
        finally
        {
            closeQuietly( in );
            closeQuietly( out );
        }

        return null;
    }

}
