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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class IdempotentCloseOutputStream
        extends OutputStream
{
    private AtomicBoolean closed = new AtomicBoolean( false );

    final private OutputStream out;

    protected IdempotentCloseOutputStream( final OutputStream out )
    {
        this.out = out;
    }

    @Override
    public void close()
            throws IOException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        if ( !closed.getAndSet( true ) ) // if previous value was false, skip this and log it!
        {
            logger.trace( "Closing: {}", this );
            out.close();
        }
        else
        {
            logger.warn( "Preventing duplicate close() call to: {}", this );
        }
    }

    @Override
    public void write( byte b[], int off, int len )
            throws IOException {
        out.write( b, off, len);
    }

    @Override
    public void write( int b )
            throws IOException {
        out.write( b );
    }
}
