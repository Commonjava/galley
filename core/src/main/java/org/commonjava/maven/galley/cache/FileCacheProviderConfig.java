package org.commonjava.maven.galley.cache;

import java.io.File;

import javax.inject.Named;

@Named
public class FileCacheProviderConfig
{
    private Boolean aliasLinking;

    private final File cacheBasedir;

    public FileCacheProviderConfig( final File cacheBasedir )
    {
        this.cacheBasedir = cacheBasedir;
    }

    public FileCacheProviderConfig withAliasLinking( final boolean aliasLinking )
    {
        this.aliasLinking = aliasLinking;
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

}
