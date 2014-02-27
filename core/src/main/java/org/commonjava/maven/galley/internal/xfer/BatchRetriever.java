/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
        if ( !hasMoreTries() )
        {
            return;
        }

        lastTry = resources.get( tries );
        logger.debug( "Try #{}: {}", tries, lastTry );
        try
        {
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
