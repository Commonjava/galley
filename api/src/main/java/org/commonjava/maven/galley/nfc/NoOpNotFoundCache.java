package org.commonjava.maven.galley.nfc;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;

@Named( "no-op-galley-nfc" )
@Alternative
public class NoOpNotFoundCache
    implements NotFoundCache
{

    @Override
    public void addMissing( final Resource resource )
    {
    }

    @Override
    public boolean isMissing( final Resource resource )
    {
        return false;
    }

    @Override
    public void clearMissing( final Location location )
    {
    }

    @Override
    public void clearMissing( final Resource resource )
    {
    }

    @Override
    public void clearAllMissing()
    {
    }

    @Override
    public Map<Location, Set<String>> getAllMissing()
    {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getMissing( final Location location )
    {
        return Collections.emptySet();
    }

}
