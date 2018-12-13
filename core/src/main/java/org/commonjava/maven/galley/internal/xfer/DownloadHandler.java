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

import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.cdi.util.weft.WeftManaged;
import org.commonjava.maven.galley.TransferContentException;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferLocationException;
import org.commonjava.maven.galley.TransferTimeoutException;
import org.commonjava.maven.galley.config.TransportManagerConfig;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class DownloadHandler
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private NotFoundCache nfc;

    @Inject
    private TransportManagerConfig config;

    private final Map<Transfer, Long> transferSizes = new ConcurrentHashMap<>();

    private final Map<Transfer, Future<DownloadJob>> pending = new HashMap<>();

    @Inject
    @WeftManaged
    @ExecutorConfig( threads = 12, named = "galley-transfers", priority = 8 )
    private ExecutorService executor;

    @SuppressWarnings( "unused" )
    public DownloadHandler()
    {
    }

    public DownloadHandler( final NotFoundCache nfc, final TransportManagerConfig config, final ExecutorService executor )
    {
        this.nfc = nfc;
        this.config = config;
        this.executor = executor;
    }

    // FIXME: download batch

    public Transfer download( final ConcreteResource resource, final Transfer target, final int timeoutSeconds,
                              final Transport transport, final boolean suppressFailures,
                              final EventMetadata eventMetadata )
            throws TransferException
    {
        if ( !resource.allowsDownloading() )
        {
            return null;
        }

        if ( transport == null )
        {
            throw new TransferLocationException( resource.getLocation(),
                                                 "No transports available to handle: {} with location type: {}",
                                                 resource, resource.getLocation().getClass().getSimpleName() );
        }

        if ( nfc.isMissing( resource ) )
        {
            logger.debug( "NFC: Already marked as missing: {}", resource );
            return null;
        }

        logger.debug( "RETRIEVE {}", resource );

        return joinOrStart( resource, target, timeoutSeconds, transport, suppressFailures, eventMetadata );
    }

    private final Object DOWNLOAD_MUTEX = new Object(); // to prevent duplicate downloads

    private Transfer joinOrStart( final ConcreteResource resource, final Transfer target, final int timeoutSeconds,
                                  final Transport transport, final boolean suppressFailures,
                                  final EventMetadata eventMetadata )
            throws TransferException
    {
        Future<DownloadJob> future;
        boolean created = false;
        synchronized ( DOWNLOAD_MUTEX )
        {
            // if the target file already exists, skip joining.
            if ( target.exists() )
            {
                return target;
            }

            future = pending.get( target );
            if ( future == null )
            {
                if ( transport == null )
                {
                    return null;
                }

                final DownloadJob job = transport.createDownloadJob( resource, target, transferSizes, timeoutSeconds, eventMetadata );

                created = true;
                future = executor.submit( job );
                logger.debug( "Created download job for path {}: {}", resource, future );
                pending.put( target, future );
            }
        }

        int waitSeconds = (int) ( timeoutSeconds * config.getTimeoutOverextensionFactor() );
        int tries = 1;
        try
        {
            while ( tries > 0 )
            {
                tries--;

                try
                {
                    logger.debug( "Waiting for download job of path: {}: {}", resource, future );
                    final DownloadJob job = future.get( waitSeconds, TimeUnit.SECONDS );

                    if ( created )
                    {
                        synchronized ( DOWNLOAD_MUTEX )
                        {
                            logger.debug( "Removing download job of path: {}: {}", resource, future );
                            pending.remove( target );
                        }
                    }

                    final Transfer downloaded = job.getTransfer();

                    if ( job.getError() != null )
                    {
                        logger.debug( "NFC: Download error. Marking as missing: {}\nError was: {}", job.getError(),
                                      resource, job.getError().getMessage() );
                        
                        if ( ! (job.getError() instanceof  TransferContentException ) )
                        {
                            nfc.addMissing( resource );
                        }

                        if ( !suppressFailures )
                        {
                            throw job.getError();
                        }
                    }
                    else if ( downloaded == null || !downloaded.exists() )
                    {
                        logger.debug( "NFC: Download did not complete. Marking as missing: {}", resource );
                        nfc.addMissing( resource );
                    }

                    return downloaded;
                }
                catch ( final InterruptedException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Download interrupted: {}", e, target );
                    }
                }
                catch ( final ExecutionException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Download failed: {}", e, target );
                    }
                }
                catch ( final TimeoutException e )
                {
                    Long size = transferSizes.get( target );
                    if ( tries > 0 )
                    {
                        continue;
                    }
                    else if ( size != null && size > config.getThresholdWaitRetrySize() )
                    {
                        logger.debug( "Downloading a large file: {}. Retrying Future.get() up to {} times.", size, tries );
                        tries = (int) ( size / config.getWaitRetryScalingIncrement() );
                        continue;
                    }
                    else if ( !suppressFailures )
                    {
                        throw new TransferTimeoutException( target, "Timed out waiting for execution of: {}", e, target );
                    }
                }
                catch ( final TransferException e )
                {
                    //noinspection ConstantConditions
                    if ( !suppressFailures )
                    {
                        throw e;
                    }
                }
                catch ( final Exception e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Download failed: {}. Reason: {}", e, resource, e.getMessage() );
                    }
                }
            }
        }
        finally
        {
            transferSizes.remove( target );
        }

        return null;
    }

}
