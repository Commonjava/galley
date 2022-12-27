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
package org.commonjava.maven.galley.spi.io;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.OverriddenBooleanValue;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

/**
 * Decorate reads and writes to a {@link Transfer}.<br/>
 *
 * <b>NOTE:</b> Anything like a {@link FilterOutputStream} or {@link FilterInputStream} that is used here to
 * wrap a {@link Transfer} stream needs to be <b>EAGER</b> about creating <b>AND LOCKING</b> derivative 
 * {@link Transfer} instances, to avoid race conditions on these metadata files. They are often accessed in 
 * close succession with the original {@link Transfer}.
 * 
 * @author jdcasey
 */
public interface TransferDecorator
{

    /**
     * Decorate a write to a {@link Transfer} instance's {@link OutputStream}.
     * 
     * <b>NOTE:</b> Anything like a {@link FilterOutputStream} or {@link FilterInputStream} that is used here to
     * wrap a {@link Transfer} stream needs to be <b>EAGER</b> about creating <b>AND LOCKING</b> derivative 
     * {@link Transfer} instances, to avoid race conditions on these metadata files. They are often accessed in 
     * close succession with the original {@link Transfer}.
     * 
     * @throws IOException
     */
    OutputStream decorateWrite( OutputStream stream, Transfer transfer, TransferOperation op, EventMetadata metadata )
        throws IOException;

    /**
     * Decorate a read from a {@link Transfer} instance's {@link OutputStream}.
     * 
     * <b>NOTE:</b> Anything like a {@link FilterOutputStream} or {@link FilterInputStream} that is used here to
     * wrap a {@link Transfer} stream needs to be <b>EAGER</b> about creating <b>AND LOCKING</b> derivative 
     * {@link Transfer} instances, to avoid race conditions on these metadata files. They are often accessed in 
     * close succession with the original {@link Transfer}.
     * 
     * @throws IOException
     */
    InputStream decorateRead( InputStream stream, Transfer transfer, EventMetadata metadata )
        throws IOException;

    void decorateTouch( Transfer transfer, EventMetadata metadata );

    OverriddenBooleanValue decorateExists( Transfer transfer, EventMetadata metadata );

    /**
     * Decorate a copy operation from one {@link Transfer} instance to another.
     * 
     * <b>NOTE:</b> Anything like a {@link FilterOutputStream} or {@link FilterInputStream} that is used here to
     * wrap a {@link Transfer} stream needs to be <b>EAGER</b> about creating <b>AND LOCKING</b> derivative 
     * {@link Transfer} instances, to avoid race conditions on these metadata files. They are often accessed in 
     * close succession with the original {@link Transfer}.
     * 
     * @throws IOException
     */
    void decorateCopyFrom( Transfer from, Transfer transfer, EventMetadata metadata )
        throws IOException;

    /**
     * Decorate deletion of a {@link Transfer} instance's underlying file.
     * 
     * <b>NOTE:</b> Implementors should be <b>EAGER</b> about creating <b>AND LOCKING</b> derivative 
     * {@link Transfer} instances, to avoid race conditions on these metadata files. They are often accessed in 
     * close succession with the original {@link Transfer}.
     * 
     * @throws IOException
     */
    void decorateDelete( Transfer transfer, EventMetadata metadata )
        throws IOException;

    String[] decorateListing( Transfer transfer, String[] listing, EventMetadata metadata )
        throws IOException;

    void decorateMkdirs( Transfer transfer, EventMetadata metadata )
        throws IOException;

    void decorateCreateFile( Transfer transfer, EventMetadata metadata )
        throws IOException;

}
