package org.commonjava.maven.galley.maven.view;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;

public interface ProjectVersionRefView
    extends ProjectRefView
{

    String getVersion();

    ProjectVersionRef asProjectVersionRef();

}
