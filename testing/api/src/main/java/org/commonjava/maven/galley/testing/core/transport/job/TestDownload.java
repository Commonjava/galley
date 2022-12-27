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
package org.commonjava.maven.galley.testing.core.transport.job;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDownload
    implements DownloadJob
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private TransferException error;

    private final byte[] data;

    private Transfer transfer;

    private EventMetadata eventMetadata;

    public TestDownload( final TransferException error )
    {
        this.data = null;
        this.error = error;
    }

    public TestDownload( final byte[] data )
    {
        this.data = data;
        this.error = null;
    }

    public TestDownload( final String classpathResource )
        throws IOException
    {
        final InputStream stream = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream( classpathResource );
        if ( stream == null )
        {
            throw new IllegalArgumentException( "classpath resource: " + classpathResource + " is missing." );
        }

        this.data = IOUtils.toByteArray( stream );
        this.error = null;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public DownloadJob call()
    {
        if ( data == null )
        {
            return null;
        }

        OutputStream stream = null;
        try
        {
            logger.info( "Writing '{}' to: {}.", new String( data ), transfer );
            stream = transfer.openOutputStream( TransferOperation.DOWNLOAD, true, eventMetadata );
            IOUtils.write( data, stream );
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
            error = new TransferException( "Failed to write: %s", e, e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }

        logger.debug( "After write, does transfer exist? {}", transfer.exists() );

        return this;
    }

    public void setTransfer( final Transfer transfer )
    {
        this.transfer = transfer;
    }

    @Override
    public long getTransferSize()
    {
        return 1;
    }

    @Override
    public Transfer getTransfer()
    {
        return transfer;
    }

    public void setEventMetadata( final EventMetadata eventMetadata )
    {
        this.eventMetadata = eventMetadata;
    }

}
