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
package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.model.SimpleLocation;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by jdcasey on 5/18/16.
 */
public class SpecialPathConstantsTest
{

    @Test
    public void defaultFileMatchesFileInRootPath()
    {
        assertThat( SpecialPathConstants.DEFAULT_FILE.getMatcher()
                                                     .matches( new SimpleLocation( "http://foo.com" ), "/file.txt" ),
                    equalTo( true ) );
    }

    @Test
    public void defaultFileMatchesPath()
    {
        assertThat( SpecialPathConstants.DEFAULT_FILE.getMatcher()
                                                     .matches( new SimpleLocation( "http://foo.com" ), "/org/commonjava/commonjava/10/commonjava-10.pom" ),
                    equalTo( true ) );
    }

    @Test
    public void defaultFileDoesntMatchRootDir()
    {
        assertThat( SpecialPathConstants.DEFAULT_FILE.getMatcher()
                                                     .matches( new SimpleLocation( "http://foo.com" ), "/" ),
                    equalTo( false ) );
    }

    @Test
    public void defaultFileDoesntMatchDirPath()
    {
        assertThat( SpecialPathConstants.DEFAULT_FILE.getMatcher()
                                                     .matches( new SimpleLocation( "http://foo.com" ), "/org/commonjava/" ),
                    equalTo( false ) );
    }

    @Test
    public void defaultDirDoesntMatchFileInRootPath()
    {
        assertThat( SpecialPathConstants.DEFAULT_DIR.getMatcher()
                                                     .matches( new SimpleLocation( "http://foo.com" ), "/file.txt" ),
                    equalTo( false ) );
    }

    @Test
    public void defaultDirDoesntMatchPath()
    {
        assertThat( SpecialPathConstants.DEFAULT_DIR.getMatcher()
                                                     .matches( new SimpleLocation( "http://foo.com" ), "/org/commonjava/commonjava/10/commonjava-10.pom" ),
                    equalTo( false ) );
    }

    @Test
    public void defaultDirMatchesRootDir()
    {
        assertThat( SpecialPathConstants.DEFAULT_DIR.getMatcher()
                                                     .matches( new SimpleLocation( "http://foo.com" ), "/" ),
                    equalTo( true ) );
    }

    @Test
    public void defaultDirMatchesDirPath()
    {
        assertThat( SpecialPathConstants.DEFAULT_DIR.getMatcher()
                                                     .matches( new SimpleLocation( "http://foo.com" ), "/org/commonjava/" ),
                    equalTo( true ) );
    }

}
