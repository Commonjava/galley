package org.commonjava.maven.galley.maven.model.view.meta;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.atlas.ident.util.VersionUtils;
import org.commonjava.maven.atlas.ident.version.SingleVersion;
import org.w3c.dom.Element;

public class VersioningView
    extends MavenMetadataElementView
{

    public VersioningView( final MavenMetadataView xmlView, final Element element )
    {
        super( xmlView, element );
    }

    public LatestSnapshotView getLatestSnapshot()
    {
        final Element e = getElement( "snapshot" );
        if ( e != null )
        {
            return new LatestSnapshotView( xmlView, e );
        }

        return null;
    }

    public List<SnapshotArtifactView> getSnapshotArtifacts()
    {
        final List<Element> elements = getElements( "snapshotVersions/snapshotVersion" );
        if ( elements != null )
        {
            final List<SnapshotArtifactView> views = new ArrayList<SnapshotArtifactView>();
            for ( final Element element : elements )
            {
                views.add( new SnapshotArtifactView( xmlView, element ) );
            }

            return views;
        }

        return null;
    }

    public List<SingleVersion> getVersions()
    {
        final List<String> versionList =
            xmlView.resolveXPathToAggregatedStringListFrom( elementContext, "versions/version", true );
        final List<SingleVersion> versions = new ArrayList<SingleVersion>();
        if ( versionList != null && !versionList.isEmpty() )
        {
            for ( final String v : versionList )
            {
                versions.add( VersionUtils.createSingleVersion( v ) );
            }
        }

        return versions;
    }

    public SingleVersion getReleaseVersion()
    {
        final Element element = getElement( "release" );
        if ( element != null )
        {
            return VersionUtils.createSingleVersion( element.getTextContent()
                                                            .trim() );
        }

        return null;
    }

    public SingleVersion getLatestVersion()
    {
        final Element element = getElement( "latest" );
        if ( element != null )
        {
            return VersionUtils.createSingleVersion( element.getTextContent()
                                                            .trim() );
        }

        return null;
    }
}
