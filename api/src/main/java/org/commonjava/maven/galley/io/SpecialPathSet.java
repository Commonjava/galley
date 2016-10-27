package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.model.SpecialPathInfo;

import java.util.List;

public interface SpecialPathSet
{
    List<SpecialPathInfo> getSpecialPathInfos();

    void registerSpecialPathInfo( SpecialPathInfo pathInfo );

    void deregisterSpecialPathInfo( SpecialPathInfo pathInfo );

    String getPackageType();
}
