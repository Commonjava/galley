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
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DownloadHandler
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private NotFoundCache nfc;

    private final Map<Transfer, Future<Transfer>> pending = new ConcurrentHashMap<Transfer, Future<Transfer>>();

    @Inject
    @ExecutorConfig( threads = 12, daemon = true, named = "galley-transfers", priority = 8 )
    private ExecutorService executor;

    public DownloadHandler()
    {
    }

    public DownloadHandler( final NotFoundCache nfc, final ExecutorService executor )
    {
        this.nfc = nfc;
        this.executor = executor;
    }

    // FIXME: download batch

    public Transfer download( final ConcreteResource resource, final Transfer target, final int timeoutSeconds, final Transport transport,
                              final boolean suppressFailures )
        throws TransferException
    {
        if ( !resource.allowsDownloading() )
        {
            return null;
        }

        if ( transport == null )
        {
            throw new TransferException( "No transports available to handle: {} with location type: {}", resource,
                                         resource.getLocation()
                                                 .getClass()
                                                 .getSimpleName() );
        }

        if ( nfc.isMissing( resource ) )
        {
            logger.debug( "NFC: Already marked as missing: {}", resource );
            return null;
        }

        logger.debug( "RETRIEVE {}", resource );

        //        if ( nfc.hasEntry( url ) )
        //        {
        //            fileEventManager.fire( new FileNotFoundEvent( repository, target.getPath() ) );
        //            return false;
        //        }

        Transfer result;
        synchronized ( pending )
        {
            result = joinDownload( target, timeoutSeconds, suppressFailures );
            if ( result == null )
            {
                result = startDownload( resource, target, timeoutSeconds, transport, suppressFailures );
            }
        }

        return result;
    }

    private Transfer joinDownload( final Transfer target, final int timeoutSeconds, final boolean suppressFailures )
        throws TransferException
    {
        // if the target file already exists, skip joining.
        if ( target.exists() )
        {
            return target;
        }
        else
        {
            final Future<Transfer> future = pending.get( target );
            if ( future != null )
            {
                Transfer f = null;
                try
                {
                    f = future.get( timeoutSeconds, TimeUnit.SECONDS );

                    return f;
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
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Timeout on: {}", e, target );
                    }
                }
            }
        }

        return null;
    }

    private Transfer startDownload( final ConcreteResource resource, final Transfer target, final int timeoutSeconds, final Transport transport,
                                    final boolean suppressFailures )
        throws TransferException
    {
        if ( target.exists() )
        {
            return target;
        }

        if ( transport == null )
        {
            return null;
        }

        final DownloadJob job = transport.createDownloadJob( resource, target, timeoutSeconds );

        final Future<Transfer> future = executor.submit( job );
        pending.put( target, future );
        try
        {
            final Transfer downloaded = future.get( timeoutSeconds, TimeUnit.SECONDS );

            if ( job.getError() != null )
            {
                logger.debug( "NFC: Download error. Marking as missing: {}\nError was: {}", job.getError(), resource, job.getError()
                                                                                                                         .getMessage() );
                nfc.addMissing( resource );

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
                throw new TransferException( "Interrupted download: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }
        catch ( final ExecutionException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed to download: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }
        finally
        {
            //            logger.info( "Marking download complete: {}", url );
            pending.remove( target );
        }

        return null;
    }

}
