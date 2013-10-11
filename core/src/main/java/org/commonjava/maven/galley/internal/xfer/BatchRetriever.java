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
import org.commonjava.util.logging.Logger;

public final class BatchRetriever
    implements Runnable
{

    private final Logger logger = new Logger( getClass() );

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
        logger.info( "Try #%d: %s", tries, lastTry );
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