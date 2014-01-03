/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.maven.internal.defaults;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.spi.defaults.AbstractMavenPluginImplications;

// TODO: Flesh out the implied artifacts!!
public class StandardMavenPluginImplications
    extends AbstractMavenPluginImplications
{

    private static final Map<ProjectRef, Set<ProjectRef>> IMPLIED_REFS;

    static
    {
        final Map<ProjectRef, Set<ProjectRef>> implied = new HashMap<ProjectRef, Set<ProjectRef>>();

        final String mavenPluginsGid = "org.apache.maven.plugins";

        final ProjectRef surefirePlugin = new ProjectRef( mavenPluginsGid, "maven-surefire-plugin" );

        final String surefireGid = "org.apache.maven.surefire";
        final Set<ProjectRef> surefire = new HashSet<ProjectRef>();
        surefire.add( new ProjectRef( surefireGid, "surefire-junit4" ) );
        surefire.add( new ProjectRef( surefireGid, "surefire-junit47" ) );
        surefire.add( new ProjectRef( surefireGid, "surefire-junit3" ) );
        surefire.add( new ProjectRef( surefireGid, "surefire-testng" ) );
        surefire.add( new ProjectRef( surefireGid, "surefire-testng-utils" ) );

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
