package org.commonjava.maven.galley.cache.pathmapped.config;

public interface PathMappedStorageConfig
{

    int getGCIntervalInMinutes();

    int getGCGracePeriodInHours();

    boolean isSubsystemEnabled( String fileSystem );

}
