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
import org.commonjava.maven.galley.TransferException;
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

    private final Map<Resource, Future<Boolean>> pending = new ConcurrentHashMap<Resource, Future<Boolean>>();

    @Inject
    @ExecutorConfig( threads = 12, daemon = true, named = "galley-transfers", priority = 8 )
    private ExecutorService executor;

    public UploadHandler()
    {
    }

    public UploadHandler( final NotFoundCache nfc, final ExecutorService executor )
    {
        this.nfc = nfc;
        this.executor = executor;
    }

    public boolean upload( final ConcreteResource resource, final InputStream stream, final long length, final String contentType,
                           final int timeoutSeconds, final Transport transport )
        throws TransferException
    {
        if ( !resource.allowsPublishing() )
        {
            throw new TransferException( "Publishing not allowed in: {}", resource );
        }

        if ( transport == null )
        {
            throw new TransferException( "No transports available to handle: {} with location type: {}", resource,
                                         resource.getLocation()
                                                 .getClass()
                                                 .getSimpleName() );
        }

        logger.debug( "PUBLISH {}", resource );

        joinUpload( resource, timeoutSeconds );

        // even if we joined another upload in progress, we still want to push this change afterward.
        return startUpload( resource, timeoutSeconds, stream, length, contentType, transport );
    }

    private boolean startUpload( final ConcreteResource resource, final int timeoutSeconds, final InputStream stream, final long length,
                                 final String contentType, final Transport transport )
        throws TransferException
    {
        if ( transport == null )
        {
            return false;
        }

        final PublishJob job = transport.createPublishJob( resource, stream, length, contentType, timeoutSeconds );

        final Future<Boolean> future = executor.submit( job );
        pending.put( resource, future );
        try
        {
            final Boolean published = future.get( timeoutSeconds, TimeUnit.SECONDS );

            if ( job.getError() != null )
            {
                throw job.getError();
            }

            nfc.clearMissing( resource );
            return published;
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
            throw new TransferException( "Timed-out publish: {}. Reason: {}", e, resource, e.getMessage() );
        }
        finally
        {
            //            logger.info( "Marking download complete: {}", url );
            pending.remove( resource );
        }
    }

    /**
     * @return true if (a) previous upload in progress succeeded, or (b) there was no previous upload.
     */
    private boolean joinUpload( final Resource resource, final int timeoutSeconds )
        throws TransferException
    {
        final Future<Boolean> future = pending.get( resource );
        if ( future != null )
        {
            Boolean f = null;
            try
            {
                f = future.get( timeoutSeconds, TimeUnit.SECONDS );

                return f;
            }
            catch ( final InterruptedException e )
            {
                throw new TransferException( "Publish interrupted: {}", e, resource );
            }
            catch ( final ExecutionException e )
            {
                throw new TransferException( "Publish failed: {}", e, resource );
            }
            catch ( final TimeoutException e )
            {
                throw new TransferException( "Timeout on: {}", e, resource );
            }
        }

        return true;
    }
}
