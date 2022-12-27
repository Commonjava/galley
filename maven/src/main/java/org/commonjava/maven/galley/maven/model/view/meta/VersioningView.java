/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.maven.model.view.meta;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.atlas.maven.ident.util.VersionUtils;
import org.commonjava.atlas.maven.ident.version.SingleVersion;
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
            final List<SnapshotArtifactView> views = new ArrayList<>();
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
        final List<SingleVersion> versions = new ArrayList<>();
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
