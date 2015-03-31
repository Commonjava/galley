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
package org.commonjava.maven.galley.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class AtomicFileOutputStreamWrapper
    extends OutputStream
{

    public static abstract class AtomicStreamCallbacks
    {
        public void beforeClose()
        {
        }

        public void afterClose()
        {
        }
    }

    private final OutputStream stream;

    private final File downloadFile;

    private final File targetFile;

    private final AtomicStreamCallbacks callbacks;

    public AtomicFileOutputStreamWrapper( final File targetFile, final File downloadFile, final OutputStream stream )
        throws FileNotFoundException
    {
        this.targetFile = targetFile;
        this.downloadFile = downloadFile;
        this.stream = stream;
        callbacks = null;
    }

    public AtomicFileOutputStreamWrapper( final File targetFile, final File downloadFile, final OutputStream stream,
                                          final AtomicStreamCallbacks callbacks )
        throws FileNotFoundException
    {
        this.targetFile = targetFile;
        this.downloadFile = downloadFile;
        this.stream = stream;
        this.callbacks = callbacks;
    }

    @Override
    public void write( final int b )
        throws IOException
    {
        stream.write( b );
    }

    @Override
    public void close()
        throws IOException
    {
        if ( callbacks != null )
        {
            callbacks.beforeClose();
        }

        stream.close();
        downloadFile.renameTo( targetFile );

        if ( callbacks != null )
        {
            callbacks.afterClose();
        }
    }

    @Override
    public void write( final byte[] b )
        throws IOException
    {
        stream.write( b );
    }

    @Override
    public void write( final byte[] b, final int off, final int len )
        throws IOException
    {
        stream.write( b, off, len );
    }

    @Override
    public void flush()
        throws IOException
    {
        stream.flush();
    }

}
