/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.maven.util;

import static org.commonjava.maven.galley.maven.ArtifactMetadataManager.DEFAULT_FILENAME;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.version.VersionSpec;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.spi.type.TypeMapper;
import org.commonjava.maven.galley.model.TypeMapping;

public final class ArtifactPathUtils
{

    private ArtifactPathUtils()
    {
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
              .append( formatVersionDirectoryPart( (ProjectVersionRef) ref ) );
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
                                              formatVersionDirectoryPart( ref ),
                                              ref.getArtifactId(), 
                                              formatVersionFilePart( ref ), 
                                              ( tm.getClassifier() == null ? "" : "-" + tm.getClassifier() ), 
                                              tm.getExtension() );
                    }
                    else
                    {
                        return String.format( "%s/%s/%s/", 
                                              src.getGroupId().replace('.', '/'), 
                                              src.getArtifactId(), 
                                              formatVersionDirectoryPart( src ) );
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
