package org.commonjava.maven.galley.cache.pathmapped.model;

import java.util.Set;

public interface ReverseMap
{
    String getFileId();

    default int getVersion()
    {
        return 0;
    }

    Set<String> getPaths();
}
