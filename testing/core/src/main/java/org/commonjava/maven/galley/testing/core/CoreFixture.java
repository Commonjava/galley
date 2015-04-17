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
