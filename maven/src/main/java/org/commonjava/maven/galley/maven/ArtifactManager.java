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
package org.commonjava.maven.galley.maven;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.maven.model.ArtifactBatch;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@SuppressWarnings( "unused" )
public interface ArtifactManager
{

    boolean delete( Location location, ArtifactRef ref )
        throws TransferException;

    boolean delete( Location location, ArtifactRef ref, EventMetadata eventMetadata )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ArtifactRef ref, EventMetadata eventMetadata )
        throws TransferException;

    ArtifactBatch batchRetrieve( ArtifactBatch batch )
        throws TransferException;

    ArtifactBatch batchRetrieve( ArtifactBatch batch, EventMetadata eventMetadata )
        throws TransferException;

    ArtifactBatch batchRetrieveAll( ArtifactBatch batch )
        throws TransferException;

    ArtifactBatch batchRetrieveAll( ArtifactBatch batch, EventMetadata eventMetadata )
        throws TransferException;

    Transfer retrieve( Location location, ArtifactRef ref )
        throws TransferException;

    Transfer retrieve( Location location, ArtifactRef ref, EventMetadata eventMetadata )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, ArtifactRef ref, EventMetadata eventMetadata )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, ArtifactRef ref, EventMetadata eventMetadata )
        throws TransferException;

    Transfer store( Location location, ArtifactRef ref, InputStream stream )
        throws TransferException;

    Transfer store( Location location, ArtifactRef ref, InputStream stream, EventMetadata eventMetadata )
        throws TransferException;

    boolean publish( Location location, ArtifactRef ref, InputStream stream, long length )
        throws TransferException;

    boolean publish( Location location, ArtifactRef ref, InputStream stream, long length, EventMetadata eventMetadata )
        throws TransferException;

    Map<TypeAndClassifier, ConcreteResource> listAvailableArtifacts( Location location, ProjectVersionRef ref )
        throws TransferException;

    Map<TypeAndClassifier, ConcreteResource> listAvailableArtifacts( Location location, ProjectVersionRef ref, EventMetadata eventMetadata )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( Location location, ProjectVersionRef ref )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( Location location, ProjectVersionRef ref, EventMetadata eventMetadata )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( List<? extends Location> locations, ProjectVersionRef ref )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( List<? extends Location> locations, ProjectVersionRef ref,
                                              EventMetadata eventMetadata )
        throws TransferException;

    List<ConcreteResource> findAllExisting( List<? extends Location> locations, ArtifactRef ref )
    throws TransferException;

    List<ConcreteResource> findAllExisting( List<? extends Location> locations , ArtifactRef ref , EventMetadata eventMetadata  )
        throws TransferException;

    ConcreteResource checkExistence( Location location, ArtifactRef ref )
    throws TransferException;

    ConcreteResource checkExistence( Location location , ArtifactRef ref , EventMetadata eventMetadata  )
        throws TransferException;

}
