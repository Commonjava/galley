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
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.SpecialPathMatcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jdcasey on 1/27/16.
 */
public class SpecialPathConstants
{
    public static final Map<SpecialPathMatcher, SpecialPathInfo> STANDARD_SPECIAL_PATHS;

    static{
        Map<SpecialPathMatcher, SpecialPathInfo> sp = new HashMap<SpecialPathMatcher, SpecialPathInfo>();

        SpecialPathInfo pi = SpecialPathInfo.from( new FilePatternMatcher( ".*\\.http-metadata\\.json$" ) )
                                            .setDecoratable( false )
                                            .setListable( false )
                                            .setPublishable( false )
                                            .setRetrievable( false )
                                            .setStorable( false )
                                            .build();

        sp.put( pi.getMatcher(), pi );

        pi = SpecialPathInfo.from( new FilePatternMatcher( "\\.listing\\.txt" ) )
                            .setDecoratable( false )
                            .setListable( false )
                            .setPublishable( false )
                            .setRetrievable( false )
                            .setStorable( false )
                            .setMergable( true )
                            .build();

        sp.put( pi.getMatcher(), pi );

        pi = SpecialPathInfo.from( new FilePatternMatcher( "maven-metadata\\.xml$" ) )
                            .setMergable( true )
                            .build();

        sp.put( pi.getMatcher(), pi );

        pi = SpecialPathInfo.from( new FilePatternMatcher( "maven-metadata\\.xml(\\.md5|\\.sha[\\d]+)$" ) )
                .setDecoratable( false )
                .setMergable( true )
                .setMetadata( true )
                .build();

        sp.put( pi.getMatcher(), pi );

        pi = SpecialPathInfo.from( new FilePatternMatcher( "archetype-catalog\\.xml$" ) )
                            .setMergable( true )
                            .build();

        sp.put( pi.getMatcher(), pi );

        pi = SpecialPathInfo.from( new FilePatternMatcher( "archetype-catalog\\.xml(\\.md5|\\.sha[\\d]+)$" ) )
                .setDecoratable( false )
                .setMergable( true )
                .setMetadata( true )
                .build();

        sp.put( pi.getMatcher(), pi );

        String notMergablePrefix = ".+(?<!(maven-metadata|archetype-catalog)\\.xml)\\.";
        for ( String extPattern : Arrays.asList( "asc$", "md5$", "sha[\\d]+$" ) )
        {
            pi = SpecialPathInfo.from( new FilePatternMatcher( notMergablePrefix + extPattern ) )
                                .setDecoratable( false )
                                .build();

            sp.put( pi.getMatcher(), pi );
        }

        STANDARD_SPECIAL_PATHS = sp;
    }
}
