package org.commonjava.maven.galley.maven.model.view;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;

public interface ProjectVersionRefView
    extends ProjectRefView
{

    String getVersion();

    ProjectVersionRef asProjectVersionRef();

}
