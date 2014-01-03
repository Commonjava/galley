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
