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

import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ListingHandler
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private NotFoundCache nfc;

    public ListingHandler()
    {
    }

    public ListingHandler( final NotFoundCache nfc )
    {
        this.nfc = nfc;
    }

    public ListingResult list( final ConcreteResource resource, final int timeoutSeconds, final Transport transport, final boolean suppressFailures )
        throws TransferException
    {
        if ( nfc.isMissing( resource ) )
        {
            logger.debug( "NFC: Already marked as missing: {}", resource );
            return null;
        }

        if ( transport == null )
        {
            return null;
        }

        logger.debug( "LIST {}", resource );

        final ListingJob job = transport.createListingJob( resource, timeoutSeconds );

        // TODO: execute this stuff in a thread just like downloads/publishes. Requires cache storage...
        try
        {
            final ListingResult result = job.call();

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

            return result;
        }
        catch ( final TimeoutException e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Timed-out download: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }
        catch ( final Exception e )
        {
            if ( !suppressFailures )
            {
                throw new TransferException( "Failed listing: {}. Reason: {}", e, resource, e.getMessage() );
            }
        }

        return null;
    }

}
