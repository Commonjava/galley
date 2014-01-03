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
import org.commonjava.util.logging.Logger;

@ApplicationScoped
public class DownloadHandler
{

    private final Logger logger = new Logger( getClass() );

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

        if ( nfc.isMissing( resource ) )
        {
            logger.info( "NFC: Already marked as missing: %s", resource );
            return null;
        }

        logger.info( "RETRIEVE %s", resource );

        //        if ( nfc.hasEntry( url ) )
        //        {
        //            fileEventManager.fire( new FileNotFoundEvent( repository, target.getPath() ) );
        //            return false;
        //        }

        Transfer result = joinDownload( target, timeoutSeconds, suppressFailures );
        if ( result == null )
        {
            result = startDownload( resource, target, timeoutSeconds, transport, suppressFailures );
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
                        throw new TransferException( "Download interrupted: %s", e, target );
                    }
                }
                catch ( final ExecutionException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Download failed: %s", e, target );
                    }
                }
                catch ( final TimeoutException e )
                {
                    if ( !suppressFailures )
                    {
                        throw new TransferException( "Timeout on: %s", e, target );
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
                logger.info( "NFC: Download error. Marking as missing: %s\nError was: %s", job.getError(), resource, job.getError()
                                                                                                                        .getMessage() );
                nfc.addMissing( resource );

                if ( !suppressFailures )
                {
                    throw job.getError();
                }
            }
            else if ( downloaded == null || !downloaded.exists() )
            {
                logger.info( "NFC: Download did not complete. Marking as missing: %s", resource );
                nfc.addMissing( resource );
            }

            return downloaded;
        }
        catch ( final InterruptedException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Interrupted download: %s. Reason: %s", e, resource, e.getMessage() );
            }
        }
        catch ( final ExecutionException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed to download: %s. Reason: %s", e, resource, e.getMessage() );
            }
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: %s. Reason: %s", e, resource, e.getMessage() );
            }
        }
        finally
        {
            //            logger.info( "Marking download complete: %s", url );
            pending.remove( target );
        }

        return null;
    }

}
