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

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.transport.PublishJob;

public class FilePublish
    implements PublishJob
{

    private final File dest;

    private final InputStream stream;

    private TransferException error;

    private boolean success;

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
    public FilePublish call()
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream( dest );
            copy( stream, out );

            success = true;
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

        return this;
    }

    @Override
    public boolean isSuccessful()
    {
        return success;
    }

}
