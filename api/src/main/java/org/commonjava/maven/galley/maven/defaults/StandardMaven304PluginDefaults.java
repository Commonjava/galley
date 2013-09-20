package org.commonjava.maven.galley.maven.defaults;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Alternative;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;

@Alternative
public class StandardMaven304PluginDefaults
    implements MavenPluginDefaults
{

    private static final String DGID = "org.apache.maven.plugins";

    private static final Map<ProjectRef, String> DEFAULT_VERSIONS = Collections.unmodifiableMap( new HashMap<ProjectRef, String>()
    {

        {
            put( new ProjectRef( DGID, "maven-resources-plugin" ), "2.4.3" );
            put( new ProjectRef( DGID, "maven-compiler-plugin" ), "2.3.2" );
            put( new ProjectRef( DGID, "maven-surefire-plugin" ), "2.7.2" );
            put( new ProjectRef( DGID, "maven-jar-plugin" ), "2.3.1" );
            put( new ProjectRef( DGID, "maven-install-plugin" ), "2.3.1" );
            put( new ProjectRef( DGID, "maven-deploy-plugin" ), "2.5" );
            put( new ProjectRef( DGID, "maven-clean-plugin" ), "2.4.1" );
            put( new ProjectRef( DGID, "maven-site-plugin" ), "2.0.1" );
            put( new ProjectRef( DGID, "maven-ejb-plugin" ), "2.3" );
            put( new ProjectRef( DGID, "maven-plugin-plugin" ), "2.7" );
            put( new ProjectRef( DGID, "maven-war-plugin" ), "2.1.1" );
            put( new ProjectRef( DGID, "maven-ear-plugin" ), "2.5" );
            put( new ProjectRef( DGID, "maven-rar-plugin" ), "2.2" );
            put( new ProjectRef( DGID, "maven-antrun-plugin" ), "1.3" );
            put( new ProjectRef( DGID, "maven-assembly-plugin" ), "2.2-beta-5" );
            put( new ProjectRef( DGID, "maven-dependency-plugin" ), "2.1" );
            put( new ProjectRef( DGID, "maven-release-plugin" ), "2.0" );
        }

        private static final long serialVersionUID = 1L;
    } );

    @Override
    public String getDefaultGroupId( final String artifactId )
    {
        return DGID;
    }

    @Override
    public String getDefaultVersion( final String groupId, final String artifactId )
    {
        return getDefaultVersion( new ProjectRef( groupId == null ? getDefaultGroupId( artifactId ) : groupId, artifactId ) );
    }

    @Override
    public String getDefaultVersion( final ProjectRef ref )
    {
        String version = DEFAULT_VERSIONS.get( ref );
        if ( version == null )
        {
            version = "LATEST";
        }

        return version;
    }

}
