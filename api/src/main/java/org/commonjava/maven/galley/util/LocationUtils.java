package org.commonjava.maven.galley.util;

import java.util.Arrays;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LocationUtils
{

    private static final Logger logger = LoggerFactory.getLogger( LocationUtils.class );

    private LocationUtils()
    {
    }

    public static int getTimeoutSeconds( final ConcreteResource resource )
    {
        logger.debug( "Retrieving timeout from resource: {}", resource );
        return getTimeoutSeconds( resource.getLocation() );
    }

    public static int getTimeoutSeconds( final Location location )
    {
        logger.debug( "Retrieving timeout from location: {}\n{}", location, Arrays.toString( Thread.currentThread()
                                                                                                   .getStackTrace() ) );
        return location.getAttribute( Location.CONNECTION_TIMEOUT_SECONDS, Integer.class,
                                      Location.DEFAULT_CONNECTION_TIMEOUT_SECONDS );
    }

}
