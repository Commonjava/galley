package org.commonjava.maven.galley.cache.pathmapped.config;

public class DefaultPathMappedStorageConfig
    implements PathMappedStorageConfig
{

    private final int defaultGCIntervalInMinutes = 60;

    private final int defaultGCGracePeriodInHours = 24;

    @Override
    public int getGCIntervalInMinutes()
    {
        return defaultGCIntervalInMinutes;
    }

    @Override
    public int getGCGracePeriodInHours() { return defaultGCGracePeriodInHours; }

    @Override
    public boolean isSubsystemEnabled( String fileSystem )
    {
        return false;
    }

}
