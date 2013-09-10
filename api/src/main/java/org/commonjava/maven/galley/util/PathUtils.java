package org.commonjava.maven.galley.util;

import static org.apache.commons.lang.StringUtils.join;
import static org.commonjava.maven.galley.ArtifactMetadataManager.DEFAULT_FILENAME;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.version.VersionSpec;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.TypeMapping;
import org.commonjava.maven.galley.type.TypeMapper;

public final class PathUtils
{

    public static final String ROOT = "/";

    private static final String[] ROOT_ARRY = { ROOT };

    private PathUtils()
    {
    }

    public static String[] parentPath( final String path )
    {
        final String[] parts = path.split( "/" );
        if ( parts.length == 1 )
        {
            return ROOT_ARRY;
        }
        else
        {
            final String[] parentParts = new String[parts.length - 1];
            System.arraycopy( parts, 0, parentParts, 0, parentParts.length );
            return parentParts;
        }
    }

    public static String normalize( final String... path )
    {
        if ( path == null || path.length < 1 )
        {
            return ROOT;
        }

        String result = join( path, "/" );
        while ( result.startsWith( "/" ) && result.length() > 1 )
        {
            result = result.substring( 1 );
        }

        return result;
    }

    public static String formatMetadataPath( final ProjectRef ref, final String filename )
        throws TransferException
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( ref.getGroupId()
                      .replace( '.', '/' ) )
          .append( '/' )
          .append( ref.getArtifactId() );

        if ( ref instanceof ProjectVersionRef )
        {
            sb.append( '/' )
              .append( PathUtils.formatVersionDirectoryPart( (ProjectVersionRef) ref ) );
        }

        sb.append( '/' )
          .append( filename == null ? DEFAULT_FILENAME : filename );

        return sb.toString();
    }

    public static String formatMetadataPath( final String groupId, final String filename )
    {
        return String.format( "%s/%s", groupId.replace( '.', '/' ), filename == null ? DEFAULT_FILENAME : filename );
    }

    public static String formatArtifactPath( final ProjectVersionRef src, final TypeMapper mapper )
        throws TransferException
    {
        /* @formatter:off */
        if ( src instanceof ArtifactRef )
        {
            final ArtifactRef ref = (ArtifactRef) src;
            final TypeMapping tm = mapper.lookup( ref.getTypeAndClassifier() );
            
            return String.format( "%s/%s/%s/%s-%s%s.%s", 
                                  ref.getGroupId().replace('.', '/'), 
                                  ref.getArtifactId(), 
                                  PathUtils.formatVersionDirectoryPart( ref ),
                                  ref.getArtifactId(), 
                                  PathUtils.formatVersionFilePart( ref ), 
                                  ( tm.getClassifier() == null ? "" : "-" + tm.getClassifier() ), 
                                  tm.getExtension() );
        }
        else
        {
            return String.format( "%s/%s/%s/", 
                                  src.getGroupId().replace('.', '/'), 
                                  src.getArtifactId(), 
                                  PathUtils.formatVersionDirectoryPart( src ) );
        }
        /* @formatter:on */
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
                     .getBaseVersion()
                     .renderStandard() + "-SNAPSHOT";
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
