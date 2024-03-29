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
package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages a list of {@link TransferDecorator} instances, and applies them in a pipeline pattern. This replaces the old
 * {@link AbstractTransferDecorator} style that used nested decorators to build the pipeline, which relied on control
 * over the {@link TransferDecorator} constructor call and didn't offer much help to CDI-managed decorators.
 */
@ApplicationScoped
public class TransferDecoratorManager
{
    @Inject
    private Instance<TransferDecorator> transferDecorators;

    private List<TransferDecorator> decorators;

    public TransferDecoratorManager()
    {
    }

    @PostConstruct
    private void initDecorators()
    {
        decorators = new ArrayList<>();
        if ( transferDecorators != null )
        {
            transferDecorators.forEach( decorator -> decorators.add( decorator ) );
        }
    }

    public TransferDecoratorManager( List<TransferDecorator> decorators )
    {
        this.decorators = decorators;
    }

    public TransferDecoratorManager( TransferDecorator... decorators )
    {
        this.decorators = Arrays.asList( decorators );
    }

    public OutputStream decorateWrite( final OutputStream stream, final Transfer transfer, final TransferOperation op,
                                       final EventMetadata metadata )
            throws IOException
    {
        OutputStream result = stream;
        for ( TransferDecorator decorator : decorators )
        {
            result = decorator.decorateWrite( result, transfer, op, metadata );
        }

        return result;
    }

    public InputStream decorateRead( final InputStream stream, final Transfer transfer, final EventMetadata metadata )
            throws IOException
    {
        Logger logger = LoggerFactory.getLogger( getClass() );

        InputStream result = stream;
        for ( TransferDecorator decorator : decorators )
        {
            logger.debug( "Decorating: {} using decorator: {}", result.getClass().getName(), decorator.getClass().getName() );
            result = decorator.decorateRead( result, transfer, metadata );
        }

        logger.debug( "Returning: {}", result.getClass().getName() );
        return result;
    }

    public void decorateTouch( final Transfer transfer, final EventMetadata metadata )
    {
        for ( TransferDecorator decorator : decorators )
        {
            decorator.decorateTouch( transfer, metadata );
        }
    }

    public OverriddenBooleanValue decorateExists( final Transfer transfer, final EventMetadata metadata )
    {
        OverriddenBooleanValue result = OverriddenBooleanValue.DEFER;
        for ( TransferDecorator decorator : decorators )
        {
            result = decorator.decorateExists( transfer, metadata );
            if ( result.overrides() )
            {
                return result;
            }
        }

        return result;
    }

    public void decorateCopyFrom( final Transfer from, final Transfer transfer, final EventMetadata metadata )
            throws IOException
    {
        for ( TransferDecorator decorator : decorators )
        {
            decorator.decorateCopyFrom( from, transfer, metadata );
        }
    }

    public void decorateDelete( final Transfer transfer, final EventMetadata metadata )
            throws IOException
    {
        for ( TransferDecorator decorator : decorators )
        {
            decorator.decorateDelete( transfer, metadata );
        }
    }

    public String[] decorateListing( final Transfer transfer, final String[] listing, final EventMetadata metadata )
            throws IOException
    {
        String[] result = listing;
        for ( TransferDecorator decorator : decorators )
        {
            result = decorator.decorateListing( transfer, result, metadata );
        }

        return result;
    }

    public void decorateMkdirs( final Transfer transfer, final EventMetadata metadata )
            throws IOException
    {
        for ( TransferDecorator decorator : decorators )
        {
            decorator.decorateMkdirs( transfer, metadata );
        }
    }

    public void decorateCreateFile( final Transfer transfer, final EventMetadata metadata )
            throws IOException
    {
        for ( TransferDecorator decorator : decorators )
        {
            decorator.decorateCreateFile( transfer, metadata );
        }
    }
}
