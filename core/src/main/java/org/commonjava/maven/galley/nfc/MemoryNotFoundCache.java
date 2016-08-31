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
package org.commonjava.maven.galley.nfc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;

@Named
@Alternative
public class MemoryNotFoundCache
    implements NotFoundCache
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Map<Location, Set<String>> missing = new HashMap<Location, Set<String>>();

    @Override
    public void addMissing( final ConcreteResource resource )
    {
        //        logger.info( "Adding to NFC: {}", resource );
        Set<String> missing = this.missing.get( resource.getLocation() );
        if ( missing == null )
        {
            missing = new HashSet<String>();
            this.missing.put( resource.getLocation(), missing );
        }

        missing.add( resource.getPath() );
    }

    @Override
    public boolean isMissing( final ConcreteResource resource )
    {
        final Set<String> missing = this.missing.get( resource.getLocation() );
        //        logger.info( "Checking NFC listing: {} for path: {} in: {}", missing, resource.getPath(), resource.getLocation() );
        return missing != null && missing.contains( resource.getPath() );
    }

    @Override
    public void clearMissing( final Location location )
    {
        //        logger.info( "Clearing from NFC: all in {}", location );
        this.missing.remove( location );
    }

    @Override
    public void clearMissing( final ConcreteResource resource )
    {
        //        logger.info( "Clearing from NFC: {}", resource );
        final Set<String> missing = this.missing.get( resource.getLocation() );
        if ( missing != null )
        {
            missing.remove( resource.getPath() );
        }
    }

    @Override
    public void clearAllMissing()
    {
        //        logger.info( "Clearing ALL from NFC" );
        this.missing.clear();
    }

    @Override
    public Map<Location, Set<String>> getAllMissing()
    {
        return missing;
    }

    @Override
    public Set<String> getMissing( final Location location )
    {
        return missing.get( location );
    }

}
