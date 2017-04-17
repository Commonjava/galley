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

import org.commonjava.maven.galley.model.FilePatternMatcher;
import org.commonjava.maven.galley.model.PathPatternMatcher;
import org.commonjava.maven.galley.model.SpecialPathInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jdcasey on 1/27/16.
 */
public class SpecialPathConstants
{

    public static final String PKG_TYPE_MAVEN = "maven";

    public static final String PKG_TYPE_GENERIC_HTTP = "generic-http";

    public static final List<SpecialPathInfo> STANDARD_SPECIAL_PATHS;

    public static final SpecialPathInfo DEFAULT_FILE = SpecialPathInfo.from( new PathPatternMatcher( ".*[^/]" ) )
                                                                      .setDecoratable( true )
                                                                      .setDeletable( true )
                                                                      .setListable( false )
                                                                      .setMergable( false )
                                                                      .setMetadata( false )
                                                                      .setPublishable( true )
                                                                      .setRetrievable( true )
                                                                      .setStorable( true )
                                                                      .build();

    public static final SpecialPathInfo DEFAULT_DIR = SpecialPathInfo.from( new PathPatternMatcher( ".*/" ) )
                                                                     .setDecoratable( true )
                                                                     .setDeletable( true )
                                                                     .setListable( false )
                                                                     .setMergable( false )
                                                                     .setMetadata( false )
                                                                     .setPublishable( true )
                                                                     .setRetrievable( true )
                                                                     .setStorable( true )
                                                                     .build();

    static
    {
        List<SpecialPathInfo> standardSp = new ArrayList<>();

        SpecialPathInfo pi = SpecialPathInfo.from( new FilePatternMatcher( ".*\\.http-metadata\\.json$" ) )
                                            .setDecoratable( false )
                                            .setListable( false )
                                            .setPublishable( false )
                                            .setRetrievable( false )
                                            .setStorable( false )
                                            .setMetadata( true )
                                            .setMergable( false )
                                            .build();

        standardSp.add( pi );

        pi = SpecialPathInfo.from( new FilePatternMatcher( "\\.listing\\.txt" ) )
                            .setDecoratable( false )
                            .setListable( false )
                            .setPublishable( false )
                            .setRetrievable( false )
                            .setStorable( false )
                            .setMergable( true )
                            .setMetadata( true )
                            .build();

        standardSp.add( pi );

        STANDARD_SPECIAL_PATHS = standardSp;
    }

    public static final SpecialPathSet MVN_SP_PATH_SET = new MavenSpecialPathSet();
}

class MavenSpecialPathSet
        implements SpecialPathSet
{
    final List<SpecialPathInfo> mvnSpecialPaths;

    MavenSpecialPathSet()
    {
        mvnSpecialPaths = new ArrayList<>();

        mvnSpecialPaths.add( SpecialPathInfo.from( new FilePatternMatcher( "maven-metadata\\.xml$" ) )
                                    .setMergable( true )
                                    .setMetadata( true )
                                    .build() );

        mvnSpecialPaths.add( SpecialPathInfo.from( new FilePatternMatcher( "maven-metadata\\.xml(\\.md5|\\.sha[\\d]+)$" ) )
                                    .setDecoratable( false )
                                    .setMergable( true )
                                    .setMetadata( true )
                                    .build() );

        mvnSpecialPaths.add( SpecialPathInfo.from( new FilePatternMatcher( "archetype-catalog\\.xml$" ) )
                                    .setMergable( true )
                                    .setMetadata( true )
                                    .build() );

        mvnSpecialPaths.add( SpecialPathInfo.from( new FilePatternMatcher( "archetype-catalog\\.xml(\\.md5|\\.sha[\\d]+)$" ) )
                                    .setDecoratable( false )
                                    .setMergable( true )
                                    .setMetadata( true )
                                    .build() );

        String notMergablePrefix = ".+(?<!(maven-metadata|archetype-catalog)\\.xml)\\.";
        for ( String extPattern : Arrays.asList( "asc$", "md5$", "sha[\\d]+$" ) )
        {
            mvnSpecialPaths.add( SpecialPathInfo.from( new FilePatternMatcher( notMergablePrefix + extPattern ) )
                                        .setDecoratable( false )
                                        .build() );
        }

    }

    @Override
    public String getPackageType()
    {
        return SpecialPathConstants.PKG_TYPE_MAVEN;
    }

    @Override
    public List<SpecialPathInfo> getSpecialPathInfos()
    {
        return mvnSpecialPaths;
    }

    @Override
    public synchronized void registerSpecialPathInfo( SpecialPathInfo pathInfo )
    {
        mvnSpecialPaths.add( pathInfo );
    }

    @Override
    public synchronized void deregisterSpecialPathInfo( SpecialPathInfo pathInfo )
    {
        mvnSpecialPaths.remove( pathInfo );
    }
}