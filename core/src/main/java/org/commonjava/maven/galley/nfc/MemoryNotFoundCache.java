/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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

@Named( "memory-galley-nfc" )
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
        return missing == null ? false : missing.contains( resource.getPath() );
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
