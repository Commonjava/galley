package org.commonjava.maven.galley.maven.view;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;

public interface ProjectRefView
{

    String getGroupId();

    String getArtifactId();

    ProjectRef asProjectRef();

}
