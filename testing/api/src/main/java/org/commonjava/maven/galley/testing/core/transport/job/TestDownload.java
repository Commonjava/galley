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
package org.commonjava.maven.galley.testing.core.transport.job;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDownload
    implements DownloadJob
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final TransferException error;

    private final byte[] data;

    private Transfer transfer;

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
    public Transfer call()
        throws Exception
    {
        if ( data == null )
        {
            return null;
        }

        OutputStream stream = null;
        try
        {
            logger.info( "Writing '{}' to: {}.", new String( data ), transfer.getDetachedFile() );
            stream = transfer.openOutputStream( TransferOperation.DOWNLOAD );
            IOUtils.write( data, stream );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }

        return transfer;
    }

    public void setTransfer( final Transfer transfer )
    {
        this.transfer = transfer;
    }

}
