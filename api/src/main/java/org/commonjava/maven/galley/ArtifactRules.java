package org.commonjava.maven.galley;

import java.util.Collection;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.util.ArtifactPathInfo;

public final class ArtifactRules
{

    private ArtifactRules()
    {
    }

    public static void checkStorageAuthorization( final Resource resource )
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

    public static Location selectStorageLocation( final String path, final Collection<? extends Location> locations )
    {
        if ( locations == null )
        {
            return null;
        }

        return selectStorageLocation( path, locations.toArray( new Location[locations.size()] ) );
    }

    public static Location selectStorageLocation( final String path, final Location... locations )
    {
        final ArtifactPathInfo pathInfo = ArtifactPathInfo.parse( path );

        Location selected = null;
        for ( final Location location : locations )
        {
            if ( !location.allowsStoring() )
            {
                continue;
            }

            //                logger.info( "Found deploy point: %s", store.getName() );
            if ( pathInfo == null )
            {
                // probably not an artifact, most likely metadata instead...
                //                    logger.info( "Selecting it for non-artifact storage: %s", path );
                selected = location;
                break;
            }
            else if ( pathInfo.isSnapshot() )
            {
                if ( location.allowsSnapshots() )
                {
                    //                        logger.info( "Selecting it for snapshot storage: %s", pathInfo );
                    selected = location;
                    break;
                }
            }
            else if ( location.allowsReleases() )
            {
                //                    logger.info( "Selecting it for release storage: %s", pathInfo );
                selected = location;
                break;
            }
        }

        return selected;
    }

}
