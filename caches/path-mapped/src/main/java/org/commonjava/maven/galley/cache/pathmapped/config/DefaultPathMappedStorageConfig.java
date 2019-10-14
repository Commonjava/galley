package org.commonjava.maven.galley.cache.pathmapped.config;

import java.util.Map;

public class DefaultPathMappedStorageConfig
                implements PathMappedStorageConfig
{
    public DefaultPathMappedStorageConfig()
    {
    }

    public DefaultPathMappedStorageConfig( Map<String, Object> properties )
    {
        this.properties = properties;
    }

    private final int defaultGCIntervalInMinutes = 60;

    private final int defaultGCGracePeriodInHours = 24;

    @Override
    public int getGCIntervalInMinutes()
    {
        return defaultGCIntervalInMinutes;
    }

    @Override
    public int getGCGracePeriodInHours()
    {
        return defaultGCGracePeriodInHours;
    }

    @Override
    public boolean isSubsystemEnabled( String fileSystem )
    {
        return false;
    }

    private Map<String, Object> properties;

    @Override
    public Object getProperty( String key )
    {
        if ( properties != null )
        {
            return properties.get( key );
        }
        return null;
    }

}
