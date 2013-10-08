package org.commonjava.maven.galley.maven.model.view;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;

public interface ProjectVersionRefView
    extends ProjectRefView
{

    String getVersion()
        throws GalleyMavenException;

    ProjectVersionRef asProjectVersionRef()
        throws GalleyMavenException;

}
