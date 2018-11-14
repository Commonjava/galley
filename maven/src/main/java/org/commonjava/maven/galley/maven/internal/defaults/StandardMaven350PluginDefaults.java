/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.maven.internal.defaults;

import org.commonjava.atlas.maven.ident.ref.ProjectRef;
import org.commonjava.atlas.maven.ident.ref.SimpleProjectRef;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Named
@Alternative
public class StandardMaven350PluginDefaults
    implements MavenPluginDefaults
{

    private static final String DGID = "org.apache.maven.plugins";

    private static final Map<ProjectRef, String> DEFAULT_VERSIONS = Collections.unmodifiableMap( new HashMap<ProjectRef, String>()
    {
        {
            put( new SimpleProjectRef( DGID, "maven-resources-plugin" ), "2.6" );
            put( new SimpleProjectRef( DGID, "maven-compiler-plugin" ), "3.1" );
            put( new SimpleProjectRef( DGID, "maven-surefire-plugin" ), "2.12.4" );
            put( new SimpleProjectRef( DGID, "maven-jar-plugin" ), "2.4" );
            put( new SimpleProjectRef( DGID, "maven-install-plugin" ), "2.4" );
            put( new SimpleProjectRef( DGID, "maven-deploy-plugin" ), "2.7" );
            put( new SimpleProjectRef( DGID, "maven-clean-plugin" ), "2.5" );
            put( new SimpleProjectRef( DGID, "maven-site-plugin" ), "3.3" );
            put( new SimpleProjectRef( DGID, "maven-ejb-plugin" ), "2.3" );
            put( new SimpleProjectRef( DGID, "maven-plugin-plugin" ), "3.2" );
            put( new SimpleProjectRef( DGID, "maven-war-plugin" ), "2.2" );
            put( new SimpleProjectRef( DGID, "maven-ear-plugin" ), "2.8" );
            put( new SimpleProjectRef( DGID, "maven-rar-plugin" ), "2.2" );
            put( new SimpleProjectRef( DGID, "maven-antrun-plugin" ), "1.7" );
            put( new SimpleProjectRef( DGID, "maven-assembly-plugin" ), "2.2-beta-5" );
            put( new SimpleProjectRef( DGID, "maven-dependency-plugin" ), "2.8" );
            put( new SimpleProjectRef( DGID, "maven-release-plugin" ), "2.3.2" );
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
        return getDefaultVersion( new SimpleProjectRef( groupId == null ? getDefaultGroupId( artifactId ) : groupId, artifactId ) );
    }

    @Override
    public String getDefaultVersion( final ProjectRef ref )
    {
        String version = DEFAULT_VERSIONS.get( ref );
        if ( version == null )
        {
            // range that will match anything, but allow selection strategy to satisfy the rough equivalent of "LATEST"
            version = "[0.0.0.1,]";
        }

        return version;
    }

}
