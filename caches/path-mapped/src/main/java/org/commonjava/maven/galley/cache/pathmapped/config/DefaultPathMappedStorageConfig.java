package org.commonjava.maven.galley.cache.pathmapped.config;

import java.util.Map;

public class DefaultPathMappedStorageConfig
                implements PathMappedStorageConfig
{
    private final int DEFAULT_GC_INTERVAL_IN_MINUTES = 60;

    private final int DEFAULT_GC_GRACE_PERIOD_IN_HOURS = 24;

    private int gcGracePeriodInHours = DEFAULT_GC_GRACE_PERIOD_IN_HOURS;

    private int gcIntervalInMinutes = DEFAULT_GC_INTERVAL_IN_MINUTES;

    public DefaultPathMappedStorageConfig()
    {
    }

    public DefaultPathMappedStorageConfig( Map<String, Object> properties )
    {
        this.properties = properties;
    }

    @Override
    public int getGCIntervalInMinutes()
    {
        return gcIntervalInMinutes;
    }

    @Override
    public int getGCGracePeriodInHours()
    {
        return gcGracePeriodInHours;
    }

    public void setGcGracePeriodInHours( int gcGracePeriodInHours )
    {
        this.gcGracePeriodInHours = gcGracePeriodInHours;
    }

    public void setGcIntervalInMinutes( int gcIntervalInMinutes )
    {
        this.gcIntervalInMinutes = gcIntervalInMinutes;
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
