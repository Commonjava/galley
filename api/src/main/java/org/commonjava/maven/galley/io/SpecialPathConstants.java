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

        SpecialPathInfo pi = SpecialPathInfo.from( new FilePatternMatcher( ".*\\.http-metadata" ) )
                                            .setDecoratable( false )
                                            .setListable( false )
                                            .setPublishable( false )
                                            .setRetrievable( false )
                                            .setStorable( false )
                                            .setDeletable( true )
                                            .build();

        sp.put( pi.getMatcher(), pi );

        pi = SpecialPathInfo.from( new FilePatternMatcher( "\\.listing\\.txt" ) )
                            .setDecoratable( false )
                            .setListable( false )
                            .setPublishable( false )
                            .setRetrievable( false )
                            .setStorable( false )
                            .build();

        sp.put( pi.getMatcher(), pi );

        for ( String extPattern: Arrays.asList( ".+\\.asc", ".+\\.md5", ".+\\.sha.*") )
        {
            pi = SpecialPathInfo.from( new FilePatternMatcher( extPattern ) )
                                .setDecoratable( false )
                                .setListable( true )
                                .setPublishable( true )
                                .setRetrievable( true )
                                .setStorable( true )
                                .build();

            sp.put( pi.getMatcher(), pi );
        }

        STANDARD_SPECIAL_PATHS = sp;
    }
}
