/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
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
