package org.commonjava.maven.galley.maven.version;

import java.util.Collections;
import java.util.List;

import org.commonjava.maven.atlas.ident.version.SingleVersion;

public final class LatestVersionSelectionStrategy
    implements VersionSelectionStrategy
{

    public static LatestVersionSelectionStrategy INSTANCE = new LatestVersionSelectionStrategy();

    private LatestVersionSelectionStrategy()
    {
    }

    @Override
    public SingleVersion select( final List<SingleVersion> candidates )
    {
        if ( candidates.isEmpty() )
        {
            return null;
        }

        Collections.sort( candidates );
        return candidates.get( candidates.size() - 1 );
    }

}
