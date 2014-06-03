/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.maven.spi.version;

import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.model.ProjectVersionRefLocation;
import org.commonjava.maven.galley.maven.version.LatestVersionSelectionStrategy;
import org.commonjava.maven.galley.maven.version.VersionSelectionStrategy;
import org.commonjava.maven.galley.model.Location;

public interface VersionResolver
{

    /**
     * Resolve the FIRST matching version in order locations are given.
     * <br/>
     * Uses {@link LatestVersionSelectionStrategy} for version selection.
     * 
     * @deprecated Use {@link VersionResolver#resolveFirstMatchVariableVersion(List, ProjectVersionRef, VersionSelectionStrategy)}
     * instead.
     */
    @Deprecated
    ProjectVersionRef resolveVariableVersions( List<? extends Location> locations, ProjectVersionRef ref )
        throws TransferException;

    /**
     * Resolve the LATEST matching version from ALL locations
     */
    ProjectVersionRef resolveLatestVariableVersion( List<? extends Location> locations, ProjectVersionRef ref,
                                                    VersionSelectionStrategy selectionStrategy )
        throws TransferException;

    /**
     * Resolve the FIRST matching version in order locations are given
     */
    ProjectVersionRef resolveFirstMatchVariableVersion( List<? extends Location> locations, ProjectVersionRef ref,
                                                        VersionSelectionStrategy selectionStrategy )
        throws TransferException;

    /**
     * Resolve the LATEST matching version from ALL locations. This will return an altered {@link ProjectVersionRef} AND
     * the location that contains it.
     */
    ProjectVersionRefLocation resolveLatestVariableVersionLocation( List<? extends Location> locations,
                                                                    ProjectVersionRef ref,
                                                                    VersionSelectionStrategy selectionStrategy )
        throws TransferException;

    /**
     * Resolve the FIRST matching version in order locations are given. This will return an altered {@link ProjectVersionRef} AND
     * the location that contains it.
     */
    ProjectVersionRefLocation resolveFirstMatchVariableVersionLocation( List<? extends Location> locations,
                                                                        ProjectVersionRef ref,
                                                                        VersionSelectionStrategy selectionStrategy )
        throws TransferException;

    /**
     * Resolve ALL matching, selected version locations from those given. This will return all altered {@link ProjectVersionRef} AND
     * the locations that contain them.
     */
    List<ProjectVersionRefLocation> resolveAllVariableVersionLocations( List<? extends Location> locations,
                                                                        ArtifactRef ref,
                                                                        VersionSelectionStrategy selectionStrategy )
        throws TransferException;

}
