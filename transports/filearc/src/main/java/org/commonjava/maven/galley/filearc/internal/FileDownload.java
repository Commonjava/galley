/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;

public class FileDownload
    implements DownloadJob
{

    private TransferException error;

    private final Transfer txfr;

    private final File src;

    private final EventMetadata eventMetadata;

    public FileDownload( final Transfer txfr, final File src, final EventMetadata eventMetadata )
    {
        this.txfr = txfr;
        this.src = src;
        this.eventMetadata = eventMetadata;
    }

    @Override
    public long getTransferSize()
    {
        return src.length();
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public DownloadJob call()
    {
        FileInputStream in = null;
        OutputStream out = null;
        try
        {
            if ( src.exists() && !src.isDirectory() )
            {
                in = new FileInputStream( src );
                out = txfr.openOutputStream( TransferOperation.DOWNLOAD, true, eventMetadata );
                copy( in, out );
            }

            return this;
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

        return this;
    }

    @Override
    public Transfer getTransfer()
    {
        return txfr;
    }
}
