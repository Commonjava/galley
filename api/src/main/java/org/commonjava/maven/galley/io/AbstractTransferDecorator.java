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
package org.commonjava.maven.galley.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.OverriddenBooleanValue;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

public abstract class AbstractTransferDecorator
    implements TransferDecorator
{

    private final TransferDecorator next;


    protected AbstractTransferDecorator()
    {
        this( null );
    }

    protected AbstractTransferDecorator( final TransferDecorator next )
    {
        this.next = next;
    }


    @Override
    @SuppressWarnings("resource")
    public final OutputStream decorateWrite( final OutputStream stream, final Transfer transfer, final TransferOperation op )
            throws IOException
    {
        OutputStream nextStream;
        if ( next == null )
        {
            nextStream = stream;
        }
        else
        {
            nextStream = next.decorateWrite( stream, transfer, op );
        }
        return decorateWriteInternal( nextStream, transfer, op );
    }

    @Override
    @SuppressWarnings("resource")
    public final InputStream decorateRead( final InputStream stream, final Transfer transfer )
            throws IOException
    {
        InputStream nextStream;
        if ( next == null )
        {
            nextStream = stream;
        }
        else
        {
            nextStream = next.decorateRead( stream, transfer );
        }
        return decorateReadInternal( nextStream, transfer );
    }

    @Override
    public final void decorateTouch( final Transfer transfer )
    {
        decorateTouchInternal( transfer );
        if ( next != null )
        {
            next.decorateTouch( transfer );
        }
    }

    @Override
    public final OverriddenBooleanValue decorateExists( final Transfer transfer )
    {
        OverriddenBooleanValue result = decorateExistsInternal( transfer );
        if ( !result.overrides() && ( next != null ) )
        {
            result = next.decorateExists( transfer );
        }
        return result;
    }

    @Override
    public final void decorateCopyFrom( final Transfer from, final Transfer transfer )
            throws IOException
    {
        decorateCopyFromInternal( from, transfer );
        if ( next != null )
        {
            next.decorateCopyFrom( from, transfer );
        }
    }

    @Override
    public final void decorateDelete( final Transfer transfer )
            throws IOException
    {
        decorateDeleteInternal( transfer );
        if ( next != null )
        {
            next.decorateDelete( transfer );
        }
    }

    @Override
    public final String[] decorateListing( final Transfer transfer, final String[] listing )
            throws IOException
    {
        final String[] result = decorateListingInternal( transfer, listing );
        if ( next == null )
        {
            return result;
        }
        else
        {
            return next.decorateListing( transfer, result );
        }
    }

    @Override
    public final void decorateMkdirs( final Transfer transfer )
            throws IOException
    {
        decorateMkdirsInternal( transfer );
        if ( next != null )
        {
            next.decorateMkdirs( transfer );
        }
    }

    @Override
    public final void decorateCreateFile( final Transfer transfer )
            throws IOException
    {
        decorateCreateFileInternal( transfer );
        if ( next != null )
        {
            next.decorateCreateFile( transfer );
        }
    }

    // ========= METHODS TO BE OVERRIDEN ==========

    @SuppressWarnings("unused")
    protected OutputStream decorateWriteInternal( final OutputStream stream, final Transfer transfer,
            final TransferOperation op )
                    throws IOException
    {
        return stream;
    }

    @SuppressWarnings("unused")
    protected InputStream decorateReadInternal( final InputStream stream, final Transfer transfer ) throws IOException
    {
        return stream;
    }

    @SuppressWarnings("unused")
    protected void decorateTouchInternal( final Transfer transfer )
    {
    }

    @SuppressWarnings("unused")
    protected OverriddenBooleanValue decorateExistsInternal( final Transfer transfer )
    {
        return OverriddenBooleanValue.DEFER;
    }

    @SuppressWarnings("unused")
    protected void decorateCopyFromInternal( final Transfer from, final Transfer transfer ) throws IOException
    {
    }

    @SuppressWarnings("unused")
    protected void decorateDeleteInternal( final Transfer transfer ) throws IOException
    {
    }

    @SuppressWarnings("unused")
    protected String[] decorateListingInternal( final Transfer transfer, final String[] listing ) throws IOException
    {
        return listing;
    }

    @SuppressWarnings("unused")
    protected void decorateMkdirsInternal( final Transfer transfer ) throws IOException
    {
    }

    @SuppressWarnings("unused")
    private void decorateCreateFileInternal( final Transfer transfer ) throws IOException
    {
    }

}
