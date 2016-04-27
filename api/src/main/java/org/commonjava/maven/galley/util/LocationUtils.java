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

    public static Integer getMaxConnections( Location location )
    {
        return location.getAttribute( Location.MAX_CONNECTIONS, Integer.class, Location.DEFAULT_MAX_CONNECTIONS );
    }

    public static int getMetadataTimeoutSeconds( final Location location )
    {
        return location.getAttribute( Location.METADATA_TIMEOUT_SECONDS, Integer.class,
                                      Location.DEFAULT_CACHE_TIMEOUT_SECONDS );
    }
}
