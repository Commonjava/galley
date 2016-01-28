package org.commonjava.maven.galley.model;

/**
 * Created by jdcasey on 1/27/16.
 */
public interface SpecialPathMatcher
{
    boolean matches( Location location, String path );
}
