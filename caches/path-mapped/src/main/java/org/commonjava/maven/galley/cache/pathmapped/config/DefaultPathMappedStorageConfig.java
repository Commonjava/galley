package org.commonjava.maven.galley.cache.pathmapped.config;

public class DefaultPathMappedStorageConfig
    implements PathMappedStorageConfig
{

    private final int defaultGCIntervalInMinutes = 60;

    @Override
    public int getGCIntervalInMinutes()
    {
        return defaultGCIntervalInMinutes;
    }

    @Override
    public boolean isSubsystemEnabled( String fileSystem )
    {
        return false;
    }

}
