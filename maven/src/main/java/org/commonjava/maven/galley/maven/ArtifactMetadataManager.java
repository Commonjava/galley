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
package org.commonjava.maven.galley.maven;

import java.io.InputStream;
import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

@SuppressWarnings( "unused" )
public interface ArtifactMetadataManager
{

    public static final String DEFAULT_FILENAME = "maven-metadata.xml";

    boolean delete( Location location, ProjectRef ref )
    throws TransferException;

    boolean delete( Location location , ProjectRef ref , EventMetadata eventMetadata  )
        throws TransferException;

    boolean delete( Location location, ProjectRef ref, String filename )
    throws TransferException;

    boolean delete( Location location , ProjectRef ref , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    boolean delete( Location location, String groupId )
    throws TransferException;

    boolean delete( Location location , String groupId , EventMetadata eventMetadata  )
        throws TransferException;

    boolean delete( Location location, String groupId, String filename )
    throws TransferException;

    boolean delete( Location location , String groupId , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, String groupId )
    throws TransferException;

    boolean deleteAll( List<? extends Location> locations , String groupId , EventMetadata eventMetadata  )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, String groupId, String filename )
    throws TransferException;

    boolean deleteAll( List<? extends Location> locations , String groupId , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ProjectRef ref )
    throws TransferException;

    boolean deleteAll( List<? extends Location> locations , ProjectRef ref , EventMetadata eventMetadata  )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ProjectRef ref, String filename )
    throws TransferException;

    boolean deleteAll( List<? extends Location> locations , ProjectRef ref , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer retrieve( Location location, String groupId )
    throws TransferException;

    Transfer retrieve( Location location , String groupId , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer retrieve( Location location, String groupId, String filename )
    throws TransferException;

    Transfer retrieve( Location location , String groupId , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer retrieve( Location location, ProjectRef ref )
    throws TransferException;

    Transfer retrieve( Location location , ProjectRef ref , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer retrieve( Location location, ProjectRef ref, String filename )
    throws TransferException;

    Transfer retrieve( Location location , ProjectRef ref , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, String groupId )
    throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations , String groupId , EventMetadata eventMetadata  )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, String groupId, String filename )
    throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations , String groupId , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, ProjectRef ref )
    throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations , ProjectRef ref , EventMetadata eventMetadata  )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, ProjectRef ref, String filename )
    throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations , ProjectRef ref , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, String groupId )
    throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations , String groupId , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, String groupId, String filename )
    throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations , String groupId , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, ProjectRef ref )
    throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations , ProjectRef ref , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, ProjectRef ref, String filename )
    throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations , ProjectRef ref , String filename , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer store( Location location, String groupId, InputStream stream )
    throws TransferException;

    Transfer store( Location location , String groupId , InputStream stream , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer store( Location location, String groupId, String filename, InputStream stream )
    throws TransferException;

    Transfer store( Location location , String groupId , String filename , InputStream stream , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer store( Location location, ProjectRef ref, InputStream stream )
    throws TransferException;

    Transfer store( Location location , ProjectRef ref , InputStream stream , EventMetadata eventMetadata  )
        throws TransferException;

    Transfer store( Location location, ProjectRef ref, String filename, InputStream stream )
    throws TransferException;

    Transfer store( Location location , ProjectRef ref , String filename , InputStream stream , EventMetadata eventMetadata  )
        throws TransferException;

    boolean publish( Location location, String groupId, InputStream stream, long length )
            throws TransferException;

    boolean publish( Location location, String groupId, InputStream stream, long length, EventMetadata metadata )
        throws TransferException;

    boolean publish( Location location, String groupId, String filename, InputStream stream, long length, String contentType )
            throws TransferException;

    boolean publish( Location location, String groupId, String filename, InputStream stream, long length, String contentType, EventMetadata metadata )
        throws TransferException;

    boolean publish( Location location, ProjectRef ref, InputStream stream, long length )
            throws TransferException;

    boolean publish( Location location, ProjectRef ref, InputStream stream, long length, EventMetadata metadata )
        throws TransferException;

    boolean publish( Location location, ProjectRef ref, String filename, InputStream stream, long length, String contentType )
            throws TransferException;

    boolean publish( Location location, ProjectRef ref, String filename, InputStream stream, long length, String contentType, EventMetadata metadata )
        throws TransferException;

}
