/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.maven;

import java.util.Collection;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.util.ArtifactPathInfo;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.VirtualResource;

public final class ArtifactRules
{

    private ArtifactRules()
    {
    }

    public static void checkStorageAuthorization( final ConcreteResource resource )
        throws TransferException
    {
        final ArtifactPathInfo pathInfo = ArtifactPathInfo.parse( resource.getPath() );
        if ( pathInfo != null && pathInfo.isSnapshot() )
        {
            if ( !resource.allowsSnapshots() )
            {
                throw new TransferException( "Cannot store snapshot in non-snapshot location: %s", resource.getLocationUri() );
            }
        }
        else if ( !resource.allowsReleases() )
        {
            throw new TransferException( "Cannot store release in snapshot-only location: %s", resource.getLocationUri() );
        }
    }

    public static ConcreteResource selectStorageResource( final VirtualResource virt )
    {
        ConcreteResource selected = null;
        for ( final ConcreteResource res : virt )
        {
            if ( res.allowsStoring() )
            {
                selected = res;
                break;
            }
        }

        return selected;
    }

    public static Location selectStorageLocation( final Collection<? extends Location> locations )
    {
        if ( locations == null )
        {
            return null;
        }

        return selectStorageLocation( locations.toArray( new Location[locations.size()] ) );
    }

    public static Location selectStorageLocation( final Location... locations )
    {
        Location selected = null;
        for ( final Location location : locations )
        {
            if ( location.allowsStoring() )
            {
                selected = location;
                break;
            }
        }

        return selected;
    }

}
