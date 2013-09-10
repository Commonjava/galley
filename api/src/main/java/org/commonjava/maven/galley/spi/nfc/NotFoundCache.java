package org.commonjava.maven.galley.spi.nfc;

import java.util.Map;
import java.util.Set;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.ConcreteResource;

public interface NotFoundCache
{

    void addMissing( ConcreteResource resource );

    boolean isMissing( ConcreteResource resource );

    void clearMissing( Location location );

    void clearMissing( ConcreteResource resource );

    void clearAllMissing();

    Map<Location, Set<String>> getAllMissing();

    Set<String> getMissing( Location location );

}
