/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
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
