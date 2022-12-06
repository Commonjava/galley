/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.maven;

import java.util.Collection;

import org.commonjava.atlas.maven.ident.util.ArtifactPathInfo;
import org.commonjava.maven.galley.TransferException;
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
                throw new TransferException( "Cannot store snapshot in non-snapshot location: %s",
                                             resource.getLocationUri() );
            }
        }
        else if ( !resource.allowsReleases() )
        {
            throw new TransferException( "Cannot store release in snapshot-only location: %s",
                                         resource.getLocationUri() );
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

        return selectStorageLocation( locations.toArray( new Location[0] ) );
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
