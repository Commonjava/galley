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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.commonjava.atlas.maven.ident.ref.ProjectRef;
import org.commonjava.atlas.maven.ident.ref.SimpleProjectRef;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.spi.defaults.AbstractMavenPluginImplications;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

// TODO: Flesh out the implied artifacts!!
@Alternative
@Named
public class StandardMavenPluginImplications
    extends AbstractMavenPluginImplications
{

    private static final Map<ProjectRef, Set<ProjectRef>> IMPLIED_REFS;

    static
    {
        final Map<ProjectRef, Set<ProjectRef>> implied = new HashMap<>();

        final String mavenPluginsGid = "org.apache.maven.plugins";

        final ProjectRef surefirePlugin = new SimpleProjectRef( mavenPluginsGid, "maven-surefire-plugin" );

        final String surefireGid = "org.apache.maven.surefire";
        final Set<ProjectRef> surefire = new HashSet<>();
        surefire.add( new SimpleProjectRef( surefireGid, "surefire-junit4" ) );
        surefire.add( new SimpleProjectRef( surefireGid, "surefire-junit47" ) );
        surefire.add( new SimpleProjectRef( surefireGid, "surefire-junit3" ) );
        surefire.add( new SimpleProjectRef( surefireGid, "surefire-testng" ) );
        surefire.add( new SimpleProjectRef( surefireGid, "surefire-testng-utils" ) );

        implied.put( surefirePlugin, Collections.unmodifiableSet( surefire ) );

        IMPLIED_REFS = Collections.unmodifiableMap( implied );
    }

    public StandardMavenPluginImplications( final XMLInfrastructure xml )
    {
        super( xml );
    }

    @Override
    protected Map<ProjectRef, Set<ProjectRef>> getImpliedRefMap()
    {
        return IMPLIED_REFS;
    }

}
