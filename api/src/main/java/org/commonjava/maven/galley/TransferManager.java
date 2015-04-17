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
