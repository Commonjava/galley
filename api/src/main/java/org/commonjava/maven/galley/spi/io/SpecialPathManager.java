package org.commonjava.maven.galley.spi.io;

import org.commonjava.maven.galley.GalleyException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.SpecialPathMatcher;
import org.commonjava.maven.galley.model.Transfer;

/**
 * Created by jdcasey on 1/27/16.
 */
public interface SpecialPathManager
{

    void registerSpecialPathInfo( SpecialPathInfo pathInfo );

    void deregisterSpecialPathInfo( SpecialPathInfo pathInfo );

    void deregisterSpecialPathInfo( SpecialPathMatcher pathMatcher );

    SpecialPathInfo getSpecialPathInfo( ConcreteResource resource );

    SpecialPathInfo getSpecialPathInfo( Transfer transfer );

    SpecialPathInfo getSpecialPathInfo( Location location, String path );
}
