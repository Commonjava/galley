package org.commonjava.maven.galley.cache;

import java.io.File;

import javax.inject.Named;

import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.util.logging.Logger;

//@Default
@Named( "dont-inject" )
public class FileCacheProviderConfig
{
    private Boolean aliasLinking;

    private final File cacheBasedir;

    private PathGenerator pathGenerator;

    protected FileCacheProviderConfig()
    {
        this( null );
    }

    public FileCacheProviderConfig( final File cacheBasedir )
    {
        new Logger( getClass() ).info( "\n\n\n\nConstructed %s with: %s\n\n\n\n", this, cacheBasedir );
        this.cacheBasedir = cacheBasedir;
    }

    public FileCacheProviderConfig withAliasLinking( final boolean aliasLinking )
    {
        this.aliasLinking = aliasLinking;
        return this;
    }

    public FileCacheProviderConfig withPathGenerator( final PathGenerator pathGenerator )
    {
        this.pathGenerator = pathGenerator;
        return this;
    }

    public boolean isAliasLinking()
    {
        return aliasLinking == null ? true : aliasLinking;
    }

    public File getCacheBasedir()
    {
        return cacheBasedir;
    }

    public PathGenerator getPathGenerator()
    {
        return pathGenerator == null ? new HashedLocationPathGenerator() : pathGenerator;
    }
}
