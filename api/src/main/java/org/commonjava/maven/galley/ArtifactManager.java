package org.commonjava.maven.galley;

import java.io.InputStream;
import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;

public interface ArtifactManager
{

    boolean delete( Location location, ArtifactRef ref )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ArtifactRef ref )
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

    TypeAndClassifier[] listAvailableArtifacts( Location location, ProjectVersionRef ref )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( Location location, ProjectVersionRef ref )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( List<? extends Location> locations, ProjectVersionRef ref )
        throws TransferException;

    List<Resource> findAllExisting( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    Resource checkExistence( Location location, ArtifactRef ref )
        throws TransferException;

}