package org.commonjava.maven.galley.io;

import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.io.PathGenerator;

public class HashPerRepoPathGenerator
    implements PathGenerator
{

    @Override
    public String getFilePath( final Location loc, final String path )
    {
        return Paths.get( formatLocationDir( loc ), path )
                    .toString();
    }

    private String formatLocationDir( final Location loc )
    {
        return DigestUtils.shaHex( loc.getUri() );
    }

}
