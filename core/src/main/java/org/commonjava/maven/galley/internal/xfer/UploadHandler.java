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
package org.commonjava.maven.galley.internal.xfer;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.cdi.util.weft.WeftManaged;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferLocationException;
import org.commonjava.maven.galley.TransferTimeoutException;
import org.commonjava.maven.galley.config.TransportManagerConfig;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UploadHandler
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private NotFoundCache nfc;

    @Inject
    private TransportManagerConfig config;

    private final Map<ConcreteResource, Long> transferSizes = new ConcurrentHashMap<>();

    private final Map<Resource, Future<PublishJob>> pending = new ConcurrentHashMap<>();

    @Inject
    @WeftManaged
    @ExecutorConfig( threads = 12, named = "galley-transfers", priority = 8 )
    private ExecutorService executor;

    @SuppressWarnings( "unused" )
    public UploadHandler()
    {
    }

    public UploadHandler( final NotFoundCache nfc, final TransportManagerConfig config, final ExecutorService executor )
    {
        this.nfc = nfc;
        this.config = config;
        this.executor = executor;
    }

    public boolean upload( final ConcreteResource resource, final InputStream stream, final long length,
                           final String contentType, final int timeoutSeconds, final Transport transport )
            throws TransferException
    {
        if ( !resource.allowsPublishing() )
        {
            throw new TransferException( "Publishing not allowed in: {}", resource );
        }

        if ( transport == null )
        {
            throw new TransferLocationException( resource.getLocation(),
                                                 "No transports available to handle: {} with location type: {}",
                                                 resource, resource.getLocation().getClass().getSimpleName() );
        }

        logger.debug( "PUBLISH {}", resource );

        return joinOrStart( resource, timeoutSeconds, stream, length, contentType, transport );
    }

    private boolean joinOrStart( final ConcreteResource resource, final int timeoutSeconds, final InputStream stream,
                                 final long length, @SuppressWarnings( "unused" ) final String contentType, final Transport transport )
            throws TransferException
    {
        if ( transport == null )
        {
            return false;
        }

        Future<PublishJob> future;
        synchronized ( pending )
        {
            future = pending.get( resource );
            if ( future == null )
            {
                final PublishJob job = transport.createPublishJob( resource, stream, length, timeoutSeconds );

                future = executor.submit( job );
                pending.put( resource, future );
            }
        }

        //int waitSeconds = (int) ( timeoutSeconds * config.getTimeoutOverextensionFactor() );
        int tries = 1;
        try
        {
            while( tries > 0 )
            {
                tries--;

                try
                {
                    final PublishJob job = future.get( timeoutSeconds, TimeUnit.SECONDS );

                    if ( job.getError() != null )
                    {
                        throw job.getError();
                    }

                    nfc.clearMissing( resource );
                    return job.isSuccessful();
                }
                catch ( final InterruptedException e )
                {
                    throw new TransferException( "Interrupted publish: {}. Reason: {}", e, resource, e.getMessage() );
                }
                catch ( final ExecutionException e )
                {
                    throw new TransferException( "Failed to publish: {}. Reason: {}", e, resource, e.getMessage() );
                }
                catch ( final TimeoutException e )
                {
                    Long size = transferSizes.get( resource );
                    if ( tries > 0 )
                    {
                    }
                    else if ( size != null && size > config.getThresholdWaitRetrySize() )
                    {
                        logger.debug( "Publishing a large file: {}. Retrying Future.get() up to {} times.", size, tries );
                        tries = (int) ( size / config.getWaitRetryScalingIncrement() );
                    }
                    else
                    {
                        throw new TransferTimeoutException( resource, "Timed out waiting for execution of: {}", e, resource );
                    }
                }
                catch ( final TransferException e )
                {
                    throw e;
                }
                catch ( final Exception e )
                {
                    throw new TransferException( "Failed listing: {}. Reason: {}", e, resource, e.getMessage() );
                }
            }
        }
        finally
        {
            transferSizes.remove( resource );

            //            logger.info( "Marking download complete: {}", url );
            pending.remove( resource );
        }

        return false;
    }
}
