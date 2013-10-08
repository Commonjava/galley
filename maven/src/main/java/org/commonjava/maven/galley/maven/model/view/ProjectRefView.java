package org.commonjava.maven.galley.maven.model.view;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;

public interface ProjectRefView
{

    String getGroupId()
        throws GalleyMavenException;

    String getArtifactId()
        throws GalleyMavenException;

    ProjectRef asProjectRef()
        throws GalleyMavenException;

}
