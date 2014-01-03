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

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

public interface ArtifactMetadataManager
{

    public static final String DEFAULT_FILENAME = "maven-metadata.xml";

    boolean delete( Location location, ProjectRef ref )
        throws TransferException;

    boolean delete( Location location, ProjectRef ref, String filename )
        throws TransferException;

    boolean delete( Location location, String groupId )
        throws TransferException;

    boolean delete( Location location, String groupId, String filename )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, String groupId )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, String groupId, String filename )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ProjectRef ref )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ProjectRef ref, String filename )
        throws TransferException;

    Transfer retrieve( Location location, String groupId )
        throws TransferException;

    Transfer retrieve( Location location, String groupId, String filename )
        throws TransferException;

    Transfer retrieve( Location location, ProjectRef ref )
        throws TransferException;

    Transfer retrieve( Location location, ProjectRef ref, String filename )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, String groupId )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, String groupId, String filename )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, ProjectRef ref )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, ProjectRef ref, String filename )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, String groupId )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, String groupId, String filename )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, ProjectRef ref )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, ProjectRef ref, String filename )
        throws TransferException;

    Transfer store( Location location, String groupId, InputStream stream )
        throws TransferException;

    Transfer store( Location location, String groupId, String filename, InputStream stream )
        throws TransferException;

    Transfer store( Location location, ProjectRef ref, InputStream stream )
        throws TransferException;

    Transfer store( Location location, ProjectRef ref, String filename, InputStream stream )
        throws TransferException;

    boolean publish( Location location, String groupId, InputStream stream, long length )
        throws TransferException;

    boolean publish( Location location, String groupId, String filename, InputStream stream, long length, String contentType )
        throws TransferException;

    boolean publish( Location location, ProjectRef ref, InputStream stream, long length )
        throws TransferException;

    boolean publish( Location location, ProjectRef ref, String filename, InputStream stream, long length, String contentType )
        throws TransferException;

}
