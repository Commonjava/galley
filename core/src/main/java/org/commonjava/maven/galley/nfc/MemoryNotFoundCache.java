package org.commonjava.maven.galley.nfc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;

@Named( "memory-galley-nfc" )
@Alternative
public class MemoryNotFoundCache
    implements NotFoundCache
{

    private final Map<Location, Set<String>> missing = new HashMap<>();

    @Override
    public void addMissing( final Resource resource )
    {
        Set<String> missing = this.missing.get( resource.getLocation() );
        if ( missing == null )
        {
            missing = new HashSet<>();
            this.missing.put( resource.getLocation(), missing );
        }

        missing.add( resource.getPath() );
    }

    @Override
    public boolean isMissing( final Resource resource )
    {
        final Set<String> missing = this.missing.get( resource.getLocation() );
        return missing == null ? false : missing.contains( resource.getPath() );
    }

    @Override
    public void clearMissing( final Location location )
    {
        this.missing.remove( location );
    }

    @Override
    public void clearMissing( final Resource resource )
    {
        final Set<String> missing = this.missing.get( resource.getLocation() );
        if ( missing != null )
        {
            missing.remove( resource.getPath() );
        }
    }

    @Override
    public void clearAllMissing()
    {
        this.missing.clear();
    }

    @Override
    public Map<Location, Set<String>> getAllMissing()
    {
        return missing;
    }

    @Override
    public Set<String> getMissing( final Location location )
    {
        return missing.get( location );
    }

}
