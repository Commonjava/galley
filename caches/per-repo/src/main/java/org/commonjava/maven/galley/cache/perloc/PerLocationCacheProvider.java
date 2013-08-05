package org.commonjava.maven.galley.cache.perloc;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.cache.CacheProvider;
import org.commonjava.maven.galley.model.Location;

public class PerLocationCacheProvider
    extends FileCacheProvider
    implements CacheProvider
{

    private final File cacheBasedir;

    public PerLocationCacheProvider( final File cacheBasedir )
    {
        super( true );
        this.cacheBasedir = cacheBasedir;
    }

    public PerLocationCacheProvider( final File cacheBasedir, final boolean aliasLinking )
    {
        super( aliasLinking );
        this.cacheBasedir = cacheBasedir;
    }

    @Override
    public String getFilePath( final Location loc, final String path )
    {
        return Paths.get( cacheBasedir.getPath(), formatLocationDir( loc ), path )
                    .toString();
    }

    private String formatLocationDir( final Location loc )
    {
        return DigestUtils.shaHex( loc.getUri() );
    }

}
