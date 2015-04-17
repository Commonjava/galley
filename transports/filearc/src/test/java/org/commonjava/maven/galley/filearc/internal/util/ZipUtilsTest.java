/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley.filearc.internal.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

public class ZipUtilsTest
{

    @Test
    public void getArchiveFileFromNakedZipUrl()
    {
        final String file = "/path/to/file.zip";
        final File f = ZipUtils.getArchiveFile( "zip://" + file );

        assertThat( f, notNullValue() );
        assertThat( f.getPath(), equalTo( file ) );
    }

    @Test
    public void getArchiveFileFromZipUrlWithSubPath()
    {
        final String file = "/path/to/file.zip";
        final String subPath = "/path/to/project.pom";
        final File f = ZipUtils.getArchiveFile( "zip://" + file + "!" + subPath );

        assertThat( f, notNullValue() );
        assertThat( f.getPath(), equalTo( file ) );
    }

    @Test
    public void getArchiveFileFromNakedJarUrl()
    {
        final String file = "/path/to/file.jar";
        final File f = ZipUtils.getArchiveFile( "jar://" + file );

        assertThat( f, notNullValue() );
        assertThat( f.getPath(), equalTo( file ) );
    }

    @Test
    public void getArchiveFileFromJarUrlWithSubPath()
    {
        final String file = "/path/to/file.jar";
        final String subPath = "/path/to/project.pom";
        final File f = ZipUtils.getArchiveFile( "jar://" + file + "!" + subPath );

        assertThat( f, notNullValue() );
        assertThat( f.getPath(), equalTo( file ) );
    }

    @Test
    public void isJarFalseFromNakedZipUrl()
    {
        final String file = "/path/to/file.zip";
        final boolean isJar = ZipUtils.isJar( "zip://" + file );

        assertThat( isJar, equalTo( false ) );
    }

    @Test
    public void isJarFalseFromZipUrlWithSubPath()
    {
        final String file = "/path/to/file.zip";
        final String subPath = "/path/to/project.pom";
        final boolean isJar = ZipUtils.isJar( "zip://" + file + "!" + subPath );

        assertThat( isJar, equalTo( false ) );
    }

    @Test
    public void isJarTrueFromNakedJarUrl()
    {
        final String file = "/path/to/file.jar";
        final boolean isJar = ZipUtils.isJar( "jar://" + file );

        assertThat( isJar, equalTo( true ) );
    }

    @Test
    public void isJarTrueFromJarUrlWithSubPath()
    {
        final String file = "/path/to/file.jar";
        final String subPath = "/path/to/project.pom";
        final boolean isJar = ZipUtils.isJar( "jar://" + file + "!" + subPath );

        assertThat( isJar, equalTo( true ) );
    }

    @Test
    public void getArchivePathNullFromNakedZipUrl()
    {
        final String file = "/path/to/file.zip";
        final String path = ZipUtils.getArchivePath( "zip://" + file );

        assertThat( path, nullValue() );
    }

    @Test
    public void getArchivePathMatchesSubFromZipUrlWithSubPath()
    {
        final String file = "/path/to/file.zip";
        final String subPath = "/path/to/project.pom";
        final String path = ZipUtils.getArchivePath( "zip://" + file + "!" + subPath );

        assertThat( path, notNullValue() );
        assertThat( path, equalTo( subPath ) );
    }

    @Test
    public void getArchivePathNullFromNakedJarUrl()
    {
        final String file = "/path/to/file.jar";
        final String path = ZipUtils.getArchivePath( "jar://" + file );

        assertThat( path, nullValue() );
    }

    @Test
    public void getArchivePathMatchesSubFromJarUrlWithSubPath()
    {
        final String file = "/path/to/file.jar";
        final String subPath = "/path/to/project.pom";
        final String path = ZipUtils.getArchivePath( "jar://" + file + "!" + subPath );

        assertThat( path, notNullValue() );
        assertThat( path, equalTo( subPath ) );
    }
}
