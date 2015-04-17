/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley.maven.testutil;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.maven.ArtifactMetadataManager;
import org.commonjava.maven.galley.maven.internal.ArtifactManagerImpl;
import org.commonjava.maven.galley.maven.internal.ArtifactMetadataManagerImpl;
import org.commonjava.maven.galley.maven.internal.type.StandardTypeMapper;
import org.commonjava.maven.galley.maven.internal.version.VersionResolverImpl;
import org.commonjava.maven.galley.maven.model.view.XPathManager;
import org.commonjava.maven.galley.maven.parse.MavenMetadataReader;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.spi.version.VersionResolver;
import org.commonjava.maven.galley.maven.util.ArtifactPathUtils;
import org.commonjava.maven.galley.testing.core.CoreFixture;

public class TestFixture
    extends CoreFixture
{

    private VersionResolver versionResolver;

    private ArtifactManager artifactManager;

    private MavenMetadataReader metadataReader;

    private XMLInfrastructure xml;

    private ArtifactMetadataManager metadataManager;

    private XPathManager xpath;

    private StandardTypeMapper typeMapper;

    public String pomPath( final ProjectVersionRef ref )
        throws Exception
    {
        return ArtifactPathUtils.formatArtifactPath( ref, typeMapper );
    }

    public String snapshotMetadataPath( final ProjectVersionRef ref )
        throws Exception
    {
        return ArtifactPathUtils.formatMetadataPath( ref, "maven-metadata.xml" );
    }

    @Override
    public void initMissingComponents()
    {
        super.initMissingComponents();

        // setup version resolver.
        if ( xml == null )
        {
            xml = new XMLInfrastructure();
        }
        if ( xpath == null )
        {
            xpath = new XPathManager();
        }

        if ( metadataManager == null )
        {
            metadataManager = new ArtifactMetadataManagerImpl( getTransfers(), getLocations() );
        }
        if ( metadataReader == null )
        {
            metadataReader = new MavenMetadataReader( xml, getLocations(), metadataManager, xpath );
        }
        if ( versionResolver == null )
        {
            versionResolver = new VersionResolverImpl( metadataReader );
        }

        if ( typeMapper == null )
        {
            typeMapper = new StandardTypeMapper();
        }

        if ( artifactManager == null )
        {
            artifactManager = new ArtifactManagerImpl( getTransfers(), getLocations(), typeMapper, versionResolver );
        }
    }

    public ArtifactManager getArtifactManager()
    {
        return artifactManager;
    }

    public void setArtifactManager( final ArtifactManager artifactManager )
    {
        this.artifactManager = artifactManager;
    }

    public VersionResolver getVersionResolver()
    {
        return versionResolver;
    }

    public void setVersionResolver( final VersionResolver versionResolver )
    {
        this.versionResolver = versionResolver;
    }

    public MavenMetadataReader getMetadataReader()
    {
        return metadataReader;
    }

    public void setMetadataReader( final MavenMetadataReader metadataReader )
    {
        this.metadataReader = metadataReader;
    }

    public XMLInfrastructure getXml()
    {
        return xml;
    }

    public void setXml( final XMLInfrastructure xml )
    {
        this.xml = xml;
    }

    public ArtifactMetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    public void setMetadataManager( final ArtifactMetadataManager metadataManager )
    {
        this.metadataManager = metadataManager;
    }

    public XPathManager getXpath()
    {
        return xpath;
    }

    public void setXpath( final XPathManager xpath )
    {
        this.xpath = xpath;
    }

}
