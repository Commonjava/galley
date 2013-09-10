package org.commonjava.maven.galley.internal.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.version.InvalidVersionSpecificationException;
import org.commonjava.maven.atlas.ident.version.SingleVersion;
import org.commonjava.maven.atlas.ident.version.VersionUtils;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.reader.MavenMetadataReader;
import org.commonjava.maven.galley.maven.view.MavenMetadataView;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.version.VersionResolver;

@ApplicationScoped
public class VersionResolverImpl
    implements VersionResolver
{

    @Inject
    private MavenMetadataReader metadataReader;

    @Override
    public ProjectVersionRef resolveVariableVersions( final List<? extends Location> locations, final ProjectVersionRef ref )
        throws TransferException
    {
        if ( !ref.getVersionSpec()
                 .isSingle() )
        {
            return resolveMulti( locations, ref );
        }
        else if ( ref.isSnapshot() )
        {
            return resolveSnapshot( locations, ref );
        }
        else
        {
            return ref;
        }
    }

    private ProjectVersionRef resolveSnapshot( final List<? extends Location> locations, final ProjectVersionRef ref )
        throws TransferException
    {
        try
        {
            final MavenMetadataView metadata = metadataReader.getMetadata( ref, locations );

            if ( metadata != null )
            {
                final String latest = metadata.resolveSingleValue( "/metadata/versioning/latest" );
                if ( latest != null )
                {
                    try
                    {
                        return ref.selectVersion( VersionUtils.createSingleVersion( latest ) );
                    }
                    catch ( final InvalidVersionSpecificationException e )
                    {
                        throw new TransferException( "Unparsable version spec found in metadata: '%s'. Reason: %s", e, latest, e.getMessage() );
                    }
                }
            }
        }
        catch ( final GalleyMavenException e )
        {
            throw new TransferException( "Failed to resolve/parse metadata for snapshot version of: %s. Reason: %s", e, ref, e.getMessage() );
        }

        return null;
    }

    // TODO: pluggable selection strategy!
    private ProjectVersionRef resolveMulti( final List<? extends Location> locations, final ProjectVersionRef ref )
        throws TransferException
    {
        final List<String> allVersions = new ArrayList<String>();
        try
        {
            final MavenMetadataView metadata = metadataReader.getMetadata( ref, locations );

            if ( metadata != null )
            {
                final List<String> versions = metadata.resolveValues( "/metadata/versioning/versions" );

                if ( versions != null )
                {
                    for ( final String version : versions )
                    {
                        if ( !allVersions.contains( version ) )
                        {
                            allVersions.add( version );
                        }
                    }
                }
            }
        }
        catch ( final GalleyMavenException e )
        {
            throw new TransferException( "Failed to resolve/parse metadata for variable version of: %s. Reason: %s", e, ref, e.getMessage() );
        }

        final LinkedList<SingleVersion> specs = new LinkedList<SingleVersion>();
        if ( allVersions != null && !allVersions.isEmpty() )
        {
            for ( final String spec : allVersions )
            {
                try
                {
                    specs.add( VersionUtils.createSingleVersion( spec ) );
                }
                catch ( final InvalidVersionSpecificationException e )
                {
                    throw new TransferException( "Unparsable version spec found in metadata: '%s'. Reason: %s", e, spec, e.getMessage() );
                }
            }
        }

        if ( !specs.isEmpty() )
        {
            Collections.sort( specs );
            SingleVersion ver = null;
            do
            {
                ver = specs.removeLast();
            }
            while ( !ver.isConcrete() );

            if ( ver != null )
            {
                return ref.selectVersion( ver );
            }
        }

        return null;
    }
}
