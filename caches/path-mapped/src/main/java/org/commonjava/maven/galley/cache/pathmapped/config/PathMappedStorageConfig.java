package org.commonjava.maven.galley.cache.pathmapped.config;

public interface PathMappedStorageConfig
{

    int getGCIntervalInMinutes();

    boolean isSubsystemEnabled( String fileSystem );

}
