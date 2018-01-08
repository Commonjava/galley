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
package org.commonjava.maven.galley.maven.spi.version;

import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.maven.model.ProjectVersionRefLocation;
import org.commonjava.maven.galley.maven.version.VersionSelectionStrategy;
import org.commonjava.maven.galley.model.Location;

@SuppressWarnings( "unused" )
public interface VersionResolver
{

    /**
     * Resolve the LATEST matching version from ALL locations
     */
    ProjectVersionRef resolveLatestVariableVersion( List<? extends Location> locations, ProjectVersionRef ref,
                                                    VersionSelectionStrategy selectionStrategy )
        throws TransferException;

    /**
     * Resolve the LATEST matching version from ALL locations
     * @param eventMetadata TODO
     */
    ProjectVersionRef resolveLatestVariableVersion( List<? extends Location> locations, ProjectVersionRef ref,
                                                    VersionSelectionStrategy selectionStrategy,
                                                    EventMetadata eventMetadata )
        throws TransferException;

    /**
     * Resolve the FIRST matching version in order locations are given
     */
    ProjectVersionRef resolveFirstMatchVariableVersion( List<? extends Location> locations, ProjectVersionRef ref,
                                                        VersionSelectionStrategy selectionStrategy )
        throws TransferException;

    /**
     * Resolve the FIRST matching version in order locations are given
     * @param eventMetadata TODO
     */
    ProjectVersionRef resolveFirstMatchVariableVersion( List<? extends Location> locations, ProjectVersionRef ref,
                                                        VersionSelectionStrategy selectionStrategy,
                                                        EventMetadata eventMetadata )
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
     * Resolve the LATEST matching version from ALL locations. This will return an altered {@link ProjectVersionRef} AND
     * the location that contains it.
     * @param eventMetadata TODO
     */
    ProjectVersionRefLocation resolveLatestVariableVersionLocation( List<? extends Location> locations,
                                                                    ProjectVersionRef ref,
                                                                    VersionSelectionStrategy selectionStrategy,
                                                                    EventMetadata eventMetadata )
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
     * Resolve the FIRST matching version in order locations are given. This will return an altered {@link ProjectVersionRef} AND
     * the location that contains it.
     * @param eventMetadata TODO
     */
    ProjectVersionRefLocation resolveFirstMatchVariableVersionLocation( List<? extends Location> locations,
                                                                        ProjectVersionRef ref,
                                                                        VersionSelectionStrategy selectionStrategy,
                                                                        EventMetadata eventMetadata )
        throws TransferException;

    /**
     * Resolve ALL matching, selected version locations from those given. This will return all altered {@link ProjectVersionRef} AND
     * the locations that contain them.
     */
    List<ProjectVersionRefLocation> resolveAllVariableVersionLocations( List<? extends Location> locations,
                                                                        ArtifactRef ref,
                                                                        VersionSelectionStrategy selectionStrategy )
        throws TransferException;

    /**
     * Resolve ALL matching, selected version locations from those given. This will return all altered {@link ProjectVersionRef} AND
     * the locations that contain them.
     * @param eventMetadata TODO
     */
    List<ProjectVersionRefLocation> resolveAllVariableVersionLocations( List<? extends Location> locations,
                                                                        ArtifactRef ref,
                                                                        VersionSelectionStrategy selectionStrategy,
                                                                        EventMetadata eventMetadata )
        throws TransferException;

}
