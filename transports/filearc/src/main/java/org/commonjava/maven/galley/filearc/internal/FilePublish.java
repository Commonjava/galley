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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.transport.PublishJob;

public class FilePublish
    implements PublishJob
{

    private final File dest;

    private final InputStream stream;

    private TransferException error;

    public FilePublish( final File dest, final InputStream stream )
    {
        this.dest = dest;
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
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream( dest );
            copy( stream, out );

            return true;
        }
        catch ( final IOException e )
        {
            error = new TransferException( "Failed to write to: %s. Reason: %s", e, dest, e.getMessage() );
        }
        finally
        {
            closeQuietly( stream );
            closeQuietly( out );
        }

        return false;
    }

}
