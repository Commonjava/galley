package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
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

    Set<Transfer> retrieveAll( List<? extends Location> locations, String groupId )
        throws TransferException;

    Set<Transfer> retrieveAll( List<? extends Location> locations, String groupId, String filename )
        throws TransferException;

    Set<Transfer> retrieveAll( List<? extends Location> locations, ProjectRef ref )
        throws TransferException;

    Set<Transfer> retrieveAll( List<? extends Location> locations, ProjectRef ref, String filename )
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

    boolean publish( Location location, String groupId, String filename, InputStream stream, long length,
                     String contentType )
        throws TransferException;

    boolean publish( Location location, ProjectRef ref, InputStream stream, long length )
        throws TransferException;

    boolean publish( Location location, ProjectRef ref, String filename, InputStream stream, long length,
                     String contentType )
        throws TransferException;

}