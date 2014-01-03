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
package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferBatch;
import org.commonjava.maven.galley.model.VirtualResource;

public interface TransferManager
{

    <T extends TransferBatch> T batchRetrieve( T batch )
        throws TransferException;

    <T extends TransferBatch> T batchRetrieveAll( T batch )
        throws TransferException;

    Transfer retrieveFirst( VirtualResource resource )
        throws TransferException;

    List<Transfer> retrieveAll( VirtualResource resource )
        throws TransferException;

    Transfer retrieve( ConcreteResource resource )
        throws TransferException;

    Transfer store( ConcreteResource resource, InputStream stream )
        throws TransferException;

    Transfer getStoreRootDirectory( Location key );

    Transfer getCacheReference( ConcreteResource resource );

    boolean deleteAll( VirtualResource resource )
        throws TransferException;

    boolean delete( ConcreteResource resource )
        throws TransferException;

    boolean publish( ConcreteResource resource, InputStream stream, long length )
        throws TransferException;

    boolean publish( ConcreteResource resource, InputStream stream, long length, String contentType )
        throws TransferException;

    ListingResult list( ConcreteResource resource )
        throws TransferException;

    List<ListingResult> listAll( VirtualResource resource )
        throws TransferException;

    boolean exists( ConcreteResource resource )
        throws TransferException;

    ConcreteResource findFirstExisting( VirtualResource resource )
        throws TransferException;

    List<ConcreteResource> findAllExisting( VirtualResource resource )
        throws TransferException;

}
