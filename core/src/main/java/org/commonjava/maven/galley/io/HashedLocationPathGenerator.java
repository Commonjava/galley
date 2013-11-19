package org.commonjava.maven.galley.io;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.apache.commons.codec.digest.DigestUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.util.PathUtils;

@Named( "hashed-location-galley-pathgen" )
@Alternative
public class HashedLocationPathGenerator
    implements PathGenerator
{

    @Override
    public String getFilePath( final ConcreteResource resource )
    {
        return PathUtils.normalize( formatLocationDir( resource.getLocation() ), resource.getPath() )
                        .toString();
    }

    private String formatLocationDir( final Location loc )
    {
        return DigestUtils.shaHex( loc.getUri() );
    }

}
