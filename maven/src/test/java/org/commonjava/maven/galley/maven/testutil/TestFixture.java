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
package org.commonjava.maven.galley.maven.testutil;

import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.maven.ArtifactMetadataManager;
import org.commonjava.maven.galley.maven.internal.ArtifactManagerImpl;
import org.commonjava.maven.galley.maven.internal.ArtifactMetadataManagerImpl;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMaven350PluginDefaults;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMavenPluginImplications;
import org.commonjava.maven.galley.maven.internal.type.StandardTypeMapper;
import org.commonjava.maven.galley.maven.internal.version.VersionResolverImpl;
import org.commonjava.maven.galley.maven.model.view.XPathManager;
import org.commonjava.maven.galley.maven.parse.MavenMetadataReader;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.rel.MavenModelProcessor;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.commonjava.maven.galley.maven.spi.version.VersionResolver;
import org.commonjava.maven.galley.maven.util.ArtifactPathUtils;
import org.commonjava.maven.galley.testing.core.CoreFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFixture
    extends CoreFixture
{

    private VersionResolver versionResolver;

    private ArtifactManager artifactManager;

    private MavenMetadataReader metadataReader;

    private MavenPomReader pomReader;

    private XMLInfrastructure xml;

    private ArtifactMetadataManager metadataManager;

    private XPathManager xpath;

    private StandardTypeMapper typeMapper;

    private MavenPluginDefaults pluginDefaults;

    private MavenPluginImplications pluginImplications;

    private MavenModelProcessor modelProcessor;

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
        throws Exception
    {
        super.initGalley();
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
            metadataManager = new ArtifactMetadataManagerImpl( getTransferManager(), getLocationExpander() );
        }
        if ( metadataReader == null )
        {
            metadataReader = new MavenMetadataReader( xml, getLocationExpander(), metadataManager, xpath );
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
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.debug(
                    "Initializing ArtifactManagerImpl using:\n  TransferManager: {}\n  LocationExpander: {}\n  TypeMapper: {}\n  VersionResolver: {}",
                    getTransferManager(), getLocationExpander(), typeMapper, versionResolver );

            artifactManager =
                    new ArtifactManagerImpl( getTransferManager(), getLocationExpander(), typeMapper, versionResolver );
        }
        else
        {
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.debug( "Using passed-in ArtifactManager instance: {}", artifactManager );
        }

        if ( pluginDefaults == null )
        {
            pluginDefaults = new StandardMaven350PluginDefaults();
        }

        if ( pluginImplications == null )
        {
            pluginImplications = new StandardMavenPluginImplications( xml );
        }

        if ( modelProcessor == null )
        {
            modelProcessor = new MavenModelProcessor();
        }

        if ( pomReader == null )
        {
            pomReader = new MavenPomReader( xml, getLocationExpander(), getArtifactManager(), getXpath(), getPluginDefaults(), getPluginImplications() );
        }
    }

    private MavenPluginImplications getPluginImplications()
    {
        return pluginImplications;
    }

    private MavenPluginDefaults getPluginDefaults()
    {
        return pluginDefaults;
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

    public void setPomReader( MavenPomReader pomReader )
    {
        this.pomReader = pomReader;
    }

    public void setTypeMapper( StandardTypeMapper typeMapper )
    {
        this.typeMapper = typeMapper;
    }

    public void setPluginDefaults( MavenPluginDefaults pluginDefaults )
    {
        this.pluginDefaults = pluginDefaults;
    }

    public void setPluginImplications( MavenPluginImplications pluginImplications )
    {
        this.pluginImplications = pluginImplications;
    }

    public MavenPomReader getPomReader()
    {
        return pomReader;
    }

    public StandardTypeMapper getTypeMapper()
    {
        return typeMapper;
    }

    public MavenModelProcessor getModelProcessor()
    {
        return modelProcessor;
    }

    public void setModelProcessor( MavenModelProcessor modelProcessor )
    {
        this.modelProcessor = modelProcessor;
    }
}
