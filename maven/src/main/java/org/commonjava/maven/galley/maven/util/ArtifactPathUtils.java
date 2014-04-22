/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
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
            throw new TransferException( "Cannot format version directory part for: '{}'. Version is compound.", ref );
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
            throw new TransferException( "Cannot format version filename part for: '{}'. Version is compound.", ref );
        }

        return vs.renderStandard();
    }

}
