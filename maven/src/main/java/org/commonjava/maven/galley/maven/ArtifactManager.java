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
package org.commonjava.maven.galley.maven;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ArtifactBatch;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

public interface ArtifactManager
{

    boolean delete( Location location, ArtifactRef ref )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    ArtifactBatch batchRetrieve( ArtifactBatch batch )
        throws TransferException;

    ArtifactBatch batchRetrieveAll( ArtifactBatch batch )
        throws TransferException;

    Transfer retrieve( Location location, ArtifactRef ref )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    Transfer store( Location location, ArtifactRef ref, InputStream stream )
        throws TransferException;

    boolean publish( Location location, ArtifactRef ref, InputStream stream, long length )
        throws TransferException;

    Map<TypeAndClassifier, ConcreteResource> listAvailableArtifacts( Location location, ProjectVersionRef ref )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( Location location, ProjectVersionRef ref )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( List<? extends Location> locations, ProjectVersionRef ref )
        throws TransferException;

    List<ConcreteResource> findAllExisting( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    ConcreteResource checkExistence( Location location, ArtifactRef ref )
        throws TransferException;

}
