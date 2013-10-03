package org.commonjava.maven.galley.maven.spi.version;

import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;

public interface VersionResolver
{

    ProjectVersionRef resolveVariableVersions( List<? extends Location> locations, ProjectVersionRef ref )
        throws TransferException;

}
