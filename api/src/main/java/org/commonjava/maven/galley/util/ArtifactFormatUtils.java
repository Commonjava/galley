package org.commonjava.maven.galley.util;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.version.VersionSpec;
import org.commonjava.maven.galley.TransferException;

public final class ArtifactFormatUtils
{

    private ArtifactFormatUtils()
    {
    }

    public static String formatVersionDirectoryPart( final ProjectVersionRef ref )
        throws TransferException
    {
        final VersionSpec vs = ref.getVersionSpec();
        if ( !vs.isSingle() )
        {
            throw new TransferException( "Cannot format version directory part for: '%s'. Version is compound.", ref );
        }

        if ( vs.isSnapshot() )
        {
            return vs.getSingleVersion()
                     .getBaseVersion() + "-SNAPSHOT";
        }
        else
        {
            return vs.renderStandard();
        }
    }

    // TODO: What about local vs. remote snapshots? We're going to need a timestamp/buildnumber to format remote filenames...
    // TODO: The CacheProvider and Transport will need different URLs for the same GAV if localized snapshot files are used!
    public static String formatVersionFilePart( final ProjectVersionRef ref )
        throws TransferException
    {
        final VersionSpec vs = ref.getVersionSpec();
        if ( !vs.isSingle() )
        {
            throw new TransferException( "Cannot format version filename part for: '%s'. Version is compound.", ref );
        }

        return vs.renderStandard();
    }

}
