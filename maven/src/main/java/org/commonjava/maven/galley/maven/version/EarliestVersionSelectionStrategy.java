package org.commonjava.maven.galley.maven.version;

import java.util.Collections;
import java.util.List;

import org.commonjava.maven.atlas.ident.version.SingleVersion;

public final class EarliestVersionSelectionStrategy
    implements VersionSelectionStrategy
{

    public static EarliestVersionSelectionStrategy INSTANCE = new EarliestVersionSelectionStrategy();

    private EarliestVersionSelectionStrategy()
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
        return candidates.get( 0 );
    }

}
