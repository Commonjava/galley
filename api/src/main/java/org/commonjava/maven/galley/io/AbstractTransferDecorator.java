/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractTransferDecorator
        implements TransferDecorator
{

    @Override
    public OutputStream decorateWrite( final OutputStream stream, final Transfer transfer, final TransferOperation op,
                                       EventMetadata metadata )
            throws IOException
    {
        return stream;
    }

    @Override
    public InputStream decorateRead( final InputStream stream, final Transfer transfer, EventMetadata metadata )
            throws IOException
    {
        return stream;
    }

    @Override
    public void decorateTouch( final Transfer transfer, EventMetadata metadata )
    {
    }

    @Override
    public OverriddenBooleanValue decorateExists( final Transfer transfer, EventMetadata metadata )
    {
        return OverriddenBooleanValue.DEFER;
    }

    @Override
    public void decorateCopyFrom( final Transfer from, final Transfer transfer, EventMetadata metadata )
            throws IOException
    {
    }

    @Override
    public void decorateDelete( final Transfer transfer, EventMetadata metadata )
            throws IOException
    {
    }

    @Override
    public String[] decorateListing( final Transfer transfer, final String[] listing, EventMetadata metadata )
            throws IOException
    {
        return listing;
    }

    @Override
    public void decorateMkdirs( final Transfer transfer, EventMetadata metadata )
            throws IOException
    {
    }

    @Override
    public void decorateCreateFile( final Transfer transfer, EventMetadata metadata )
            throws IOException
    {
    }

}
