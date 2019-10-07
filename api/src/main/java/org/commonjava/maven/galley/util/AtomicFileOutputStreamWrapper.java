/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtomicFileOutputStreamWrapper
    extends IdempotentCloseOutputStream
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

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final File downloadFile;

    private final File targetFile;

    private final AtomicStreamCallbacks callbacks;

    public AtomicFileOutputStreamWrapper( final File targetFile, final File downloadFile, final OutputStream stream )
        throws FileNotFoundException
    {
        super( stream );
        this.targetFile = targetFile;
        this.downloadFile = downloadFile;
        callbacks = null;
    }

    public AtomicFileOutputStreamWrapper( final File targetFile, final File downloadFile, final OutputStream stream,
                                          final AtomicStreamCallbacks callbacks )
        throws FileNotFoundException
    {
        super( stream );
        this.targetFile = targetFile;
        this.downloadFile = downloadFile;
        this.callbacks = callbacks;
    }

    @Override
    public void close()
        throws IOException
    {
        if ( callbacks != null )
        {
            callbacks.beforeClose();
        }

        try
        {
            super.close();
        }
        finally
        {
            try
            {
                downloadFile.renameTo( targetFile );
            }
            catch ( final Exception e )
            {
                logger.error( String.format( "Failed to rename: %s to: %s", downloadFile, targetFile ), e );
            }

            if ( callbacks != null )
            {
                callbacks.afterClose();
            }
        }
    }

}
