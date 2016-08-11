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
package org.commonjava.maven.galley.internal.xfer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.VirtualResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BatchRetriever
        implements Callable<BatchRetriever>
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final TransferManager xfer;

    private final List<ConcreteResource> resources;

    private int tries = 0;

    private Transfer transfer;

    private TransferException error;

    private ConcreteResource lastTry;

    private final boolean suppressFailures;

    private final Resource rootResource;

    private final EventMetadata eventMetadata;

    public BatchRetriever( final TransferManager xfer, final Resource resource, final boolean suppressFailures,
                           final EventMetadata eventMetadata )
    {
        this.xfer = xfer;
        this.suppressFailures = suppressFailures;
        this.rootResource = resource;
        this.eventMetadata = eventMetadata;
        if ( resource instanceof ConcreteResource )
        {
            resources = Collections.singletonList( (ConcreteResource) resource );
        }
        else
        {
            resources = ( (VirtualResource) resource ).toConcreteResources();
        }
    }

    @Override
    public BatchRetriever call()
    {
        String oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName( "BATCH-TRY#" + tries + "@" + rootResource );
        try
        {
            if ( !hasMoreTries() )
            {
                logger.debug( "Out of tries for: {}. Last try was: {}. Returning.", rootResource, lastTry );
                return this;
            }

            lastTry = resources.get( tries );
            logger.debug( "Try #{} in {}: {}", tries, rootResource, lastTry );

            transfer = xfer.retrieve( lastTry, suppressFailures, eventMetadata );
        }
        catch ( final TransferException e )
        {
            error = e;
        }
        finally
        {
            logger.debug( "Try #{} finishing up for: {}. Last try was: {}", tries, rootResource, lastTry );
            tries++;
            Thread.currentThread().setName( oldThreadName );
        }

        return this;
    }

    public boolean hasMoreTries()
    {
        return resources.size() > tries;
    }

    public ConcreteResource getLastTry()
    {
        return lastTry;
    }

    public TransferException getError()
    {
        return error;
    }

    public Transfer getTransfer()
    {
        return transfer;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( rootResource == null ) ? 0 : rootResource.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final BatchRetriever other = (BatchRetriever) obj;
        if ( rootResource == null )
        {
            if ( other.rootResource != null )
            {
                return false;
            }
        }
        else if ( !rootResource.equals( other.rootResource ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return String.format( "BatchRetriever [executed tries=%s, lastTry=%s, rootResource=%s]", tries, lastTry,
                              rootResource );
    }

}
