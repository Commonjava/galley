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
import java.util.concurrent.CountDownLatch;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferManagerImpl;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.VirtualResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BatchRetriever
    implements Runnable
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final TransferManagerImpl xfer;

    private final List<ConcreteResource> resources;

    private int tries = 0;

    private Transfer transfer;

    private TransferException error;

    private CountDownLatch latch;

    private ConcreteResource lastTry;

    private final boolean suppressFailures;

    public BatchRetriever( final TransferManagerImpl xfer, final Resource resource, final boolean suppressFailures )
    {
        this.xfer = xfer;
        this.suppressFailures = suppressFailures;
        if ( resource instanceof ConcreteResource )
        {
            resources = Collections.singletonList( (ConcreteResource) resource );
        }
        else
        {
            resources = ( (VirtualResource) resource ).toConcreteResources();
        }
    }

    public void setLatch( final CountDownLatch latch )
    {
        this.latch = latch;
    }

    @Override
    public void run()
    {
        try
        {
            if ( !hasMoreTries() )
            {
                return;
            }

            lastTry = resources.get( tries );
            logger.debug( "Try #{}: {}", tries, lastTry );

            transfer = xfer.retrieve( lastTry, suppressFailures );
        }
        catch ( final TransferException e )
        {
            error = e;
        }
        finally
        {
            tries++;
            latch.countDown();
        }
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

}
