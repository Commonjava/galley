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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;

public class ZipDownload
    extends AbstractZipOperation
    implements DownloadJob
{

    private TransferException error;

    private final EventMetadata eventMetadata;

    public ZipDownload( final Transfer txfr, final EventMetadata eventMetadata )
    {
        super( txfr );
        this.eventMetadata = eventMetadata;
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
    public DownloadJob call()
    {
        final File src = getZipFile();

        if ( src.isDirectory() )
        {
            return this;
        }

        ZipFile zf = null;
        InputStream in = null;
        OutputStream out = null;
        try
        {
            zf = isJarOperation() ? new JarFile( src ) : new ZipFile( src );

            final ZipEntry entry = zf.getEntry( getFullPath() );
            if ( entry != null )
            {
                if ( entry.isDirectory() )
                {
                    error =
                        new TransferException( "Cannot read stream. Source is a directory: %s!%s",
                                               getLocation().getUri(), getFullPath() );
                }
                else
                {
                    in = zf.getInputStream( entry );
                    out = getTransfer().openOutputStream( TransferOperation.DOWNLOAD, true, eventMetadata );

                    copy( in, out );

                    return this;
                }
            }
            else
            {
                error = new TransferException( "Cannot find entry: %s in: %s", getFullPath(), getLocation()
                                                                                                     .getUri() );
            }
        }
        catch ( final IOException e )
        {
            error =
                new TransferException( "Failed to copy from: %s to: %s. Reason: %s", e, src, getTransfer(),
                                       e.getMessage() );
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

        if ( error != null )
        {
            logger.error( "Failed to download: {}. Reason: {}", this, error );
        }

        return null;
    }

    @Override
    public Transfer getTransfer()
    {
        return super.getTransfer();
    }
}
