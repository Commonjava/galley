/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.maven.util;

import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


public class PomPeekTest
{

    private static final String BASE = "pom-peek/";

    @Test
    public void findModules()
    {
        final File pom = getResourceFile( BASE + "contains-modules.pom" );
        final PomPeek peek = new PomPeek( pom );
        final Set<String> modules = peek.getModules();
        assertThat( modules, notNullValue() );
        assertThat( modules.size(), equalTo( 2 ) );
        assertThat( modules.contains( "child1" ), equalTo( true ) );
        assertThat( modules.contains( "child2" ), equalTo( true ) );
        assertThat( modules.contains( "child3" ), equalTo( false ) );
    }

    @Test
    public void findGAVDirectlyInProjectAtTop()
    {
        final File pom = getResourceFile( BASE + "direct-gav-at-top.pom" );
        final PomPeek peek = new PomPeek( pom );

        assertThat( peek.getKey(), notNullValue() );

        final ProjectVersionRef key = peek.getKey();
        assertThat( key.getGroupId(), equalTo( "test" ) );
        assertThat( key.getArtifactId(), equalTo( "direct-gav-at-top" ) );
        assertThat( key.getVersionString(), equalTo( "1" ) );

    }

    @Test
    public void findGAVDirectlyInProjectBelowProperties()
    {
        final File pom = getResourceFile( BASE + "direct-gav-below-props.pom" );
        final PomPeek peek = new PomPeek( pom );

        assertThat( peek.getKey(), notNullValue() );

        final ProjectVersionRef key = peek.getKey();
        assertThat( key.getGroupId(), equalTo( "test" ) );
        assertThat( key.getArtifactId(), equalTo( "direct-gav-below-props" ) );
        assertThat( key.getVersionString(), equalTo( "1" ) );

    }

    @Test
    public void findGAVInheritedFromParentAtTop()
    {
        final File pom = getResourceFile( BASE + "inherited-gav-at-top.pom" );
        final PomPeek peek = new PomPeek( pom );

        assertThat( peek.getKey(), notNullValue() );

        final ProjectVersionRef key = peek.getKey();
        assertThat( key.getGroupId(), equalTo( "test" ) );
        assertThat( key.getArtifactId(), equalTo( "inherited-gav-at-top" ) );
        assertThat( key.getVersionString(), equalTo( "1" ) );

    }

    @Test
    public void findGAVInheritedFromParentWithVersionOverrideAtTop()
    {
        final File pom = getResourceFile( BASE + "inherited-gav-with-override-at-top.pom" );
        final PomPeek peek = new PomPeek( pom );

        assertThat( peek.getKey(), notNullValue() );

        final ProjectVersionRef key = peek.getKey();
        assertThat( key.getGroupId(), equalTo( "test" ) );
        assertThat( key.getArtifactId(), equalTo( "inherited-gav-with-override-at-top" ) );
        assertThat( key.getVersionString(), equalTo( "2" ) );

    }

    @Test
    public void findGAVInheritedFromParentWithGroupAndVersionOverrideAtTop() throws Exception
    {
        final File pom = getResourceFile(BASE + "inherited-gav-with-group-override-at-top.pom");
        final PomPeek peek = new PomPeek( pom );

        final ProjectVersionRef key = peek.getKey();

        assertThat( key, notNullValue() );

        assertThat( key.getGroupId(), equalTo( "a-different-test-group" ) );
        assertThat( key.getArtifactId(), equalTo( "inherited-gav-with-group-override-at-top" ) );
        assertThat( key.getVersionString(), equalTo( "1" ) );

    }

    // Utility functions

    public static File getResourceFile( final String path )
    {
        final URL resource = Thread.currentThread()
                                   .getContextClassLoader()
                                   .getResource( path );
        if ( resource == null )
        {
            // fail( "Resource not found: " + path );
        }

        return new File( resource.getPath() );
    }


}
