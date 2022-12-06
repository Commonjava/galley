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

import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.TransferTimeoutException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.util.TransferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ExistenceHandler
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private NotFoundCache nfc;

    @SuppressWarnings( "unused" )
    public ExistenceHandler()
    {
    }

    public ExistenceHandler( final NotFoundCache nfc )
    {
        this.nfc = nfc;
    }

    public boolean exists( final ConcreteResource resource, final Transfer transfer, final int timeoutSeconds,
                           final Transport transport, final boolean suppressFailures )
        throws TransferException
    {
        if ( nfc.isMissing( resource ) )
        {
            logger.debug( "NFC: Already marked as missing: {}", resource );
            return false;
        }

        if ( TransferUtils.filterTransfer( transfer ) )
        {
            logger.info( "Transfer has been filtered by transfer's metadata settings" );
            return false;
        }

        if ( transport == null )
        {
            logger.warn( "No transports available to handle: {} with location type: {}", resource,
                         resource.getLocation()
                                 .getClass()
                                 .getSimpleName() );
            return false;
        }

        logger.debug( "EXISTS {}", resource );

        final ExistenceJob job = transport.createExistenceJob( resource, transfer, timeoutSeconds );

        // TODO: execute this stuff in a thread just like downloads/publishes. Requires cache storage...
        try
        {
            final Boolean result = job.call();

            if ( job.getError() != null )
            {
                logger.debug( "NFC: Download error. Marking as missing: {}", resource );
                nfc.addMissing( resource );

                if ( !suppressFailures )
                {
                    throw job.getError();
                }
            }
            else if ( result == null )
            {
                logger.debug( "NFC: Download did not complete. Marking as missing: {}", resource );
                nfc.addMissing( resource );
            }
            else if ( !result )
            {
                logger.debug( "NFC: Existence check returned false. Marking as missing: {}", resource );
                nfc.addMissing( resource );
            }

            return result != null && result;
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferTimeoutException( transfer, "Timed-out existence check: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }
        catch ( final TransferException e )
        {
            if ( !suppressFailures )
            {
                throw e;
            }
        }
        catch ( final Exception e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed existence check: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }

        return false;
    }

}
