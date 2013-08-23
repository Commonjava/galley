package org.commonjava.maven.galley.spi.nfc;

import java.util.Map;
import java.util.Set;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;

public interface NotFoundCache
{

    void addMissing( Resource resource );

    boolean isMissing( Resource resource );

    void clearMissing( Location location );

    void clearMissing( Resource resource );

    void clearAllMissing();

    Map<Location, Set<String>> getAllMissing();

    Set<String> getMissing( Location location );

}
