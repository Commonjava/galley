package org.commonjava.maven.galley.model;

/**
 * Created by jdcasey on 1/27/16.
 */
public class PathPatternMatcher
    implements SpecialPathMatcher
{
    private String pattern;

    public PathPatternMatcher( String pattern )
    {
        this.pattern = pattern;
    }

    @Override
    public boolean matches( Location location, String path )
    {
        return path == null ? false : path.matches( pattern );
    }
}
