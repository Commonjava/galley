package org.commonjava.maven.galley.maven.spi.defaults;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;

public interface MavenPluginDefaults
{

    public String getDefaultGroupId( String artifactId );

    public String getDefaultVersion( String groupId, String artifactId );

    public String getDefaultVersion( ProjectRef ref );

}
