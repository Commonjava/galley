package org.commonjava.maven.galley.model;

import java.io.File;

/**
 * Created by jdcasey on 1/27/16.
 */
public class FilePatternMatcher
    implements SpecialPathMatcher
{
    private String pattern;

    public FilePatternMatcher( String pattern )
    {
        this.pattern = pattern;
    }

    @Override
    public boolean matches( Location location, String path )
    {
        return path == null ? false : new File( path).getName().matches( pattern );
    }
}
