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
package org.commonjava.maven.galley.testing.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.commonjava.maven.galley.TransferManagerImpl;
import org.commonjava.maven.galley.internal.xfer.DownloadHandler;
import org.commonjava.maven.galley.internal.xfer.ExistenceHandler;
import org.commonjava.maven.galley.internal.xfer.ListingHandler;
import org.commonjava.maven.galley.internal.xfer.UploadHandler;
import org.commonjava.maven.galley.transport.TransportManagerImpl;
import org.junit.rules.TemporaryFolder;

public class CoreFixture
    extends ApiFixture
{

    public CoreFixture()
    {
        super();
    }

    public CoreFixture( final TemporaryFolder temp )
    {
        super( temp );
    }

    @Override
    public void initMissingComponents()
    {
        super.initMissingComponents();

        if ( getTransports() == null )
        {
            setTransports( new TransportManagerImpl( getTransport() ) );
        }

        final ExecutorService executor = Executors.newFixedThreadPool( 2 );
        final ExecutorService batchExecutor = Executors.newFixedThreadPool( 2 );

        final DownloadHandler dh = new DownloadHandler( getNfc(), executor );
        final UploadHandler uh = new UploadHandler( getNfc(), executor );
        final ListingHandler lh = new ListingHandler( getNfc() );
        final ExistenceHandler eh = new ExistenceHandler( getNfc() );

        if ( getTransfers() == null )
        {
            setTransfers( new TransferManagerImpl( getTransports(), getCache(), getNfc(), getEvents(), dh, uh, lh, eh, batchExecutor ) );
        }

        super.initMissingComponents();
    }

}
