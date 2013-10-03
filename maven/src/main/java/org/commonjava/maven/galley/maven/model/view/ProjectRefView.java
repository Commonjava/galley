package org.commonjava.maven.galley.maven.model.view;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;

public interface ProjectRefView
{

    String getGroupId();

    String getArtifactId();

    ProjectRef asProjectRef();

}
