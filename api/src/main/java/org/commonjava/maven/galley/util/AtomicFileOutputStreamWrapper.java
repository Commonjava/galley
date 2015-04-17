/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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
