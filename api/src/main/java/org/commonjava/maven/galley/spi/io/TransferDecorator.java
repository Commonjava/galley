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
package org.commonjava.maven.galley.spi.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

public interface TransferDecorator
{

    OutputStream decorateWrite( OutputStream stream, Transfer transfer, TransferOperation op )
        throws IOException;

    InputStream decorateRead( InputStream stream, Transfer transfer )
        throws IOException;

    void decorateTouch( Transfer transfer );

    void decorateExists( Transfer transfer );

    void decorateCopyFrom( Transfer from, Transfer transfer )
        throws IOException;

    void decorateDelete( Transfer transfer )
        throws IOException;

    String[] decorateListing( Transfer transfer, String[] listing )
        throws IOException;

    void decorateMkdirs( Transfer transfer )
        throws IOException;

    void decorateCreateFile( Transfer transfer )
        throws IOException;

}
