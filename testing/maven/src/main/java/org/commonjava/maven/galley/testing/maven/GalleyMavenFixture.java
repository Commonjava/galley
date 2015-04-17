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
package org.commonjava.maven.galley.testing.maven;

import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.maven.ArtifactMetadataManager;
import org.commonjava.maven.galley.maven.internal.ArtifactManagerImpl;
import org.commonjava.maven.galley.maven.internal.ArtifactMetadataManagerImpl;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMaven304PluginDefaults;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMavenPluginImplications;
import org.commonjava.maven.galley.maven.internal.type.StandardTypeMapper;
import org.commonjava.maven.galley.maven.internal.version.VersionResolverImpl;
import org.commonjava.maven.galley.maven.model.view.XPathManager;
import org.commonjava.maven.galley.maven.parse.MavenMetadataReader;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.commonjava.maven.galley.maven.spi.type.TypeMapper;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.testing.core.ApiFixture;
import org.commonjava.maven.galley.testing.core.cache.TestCacheProvider;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class GalleyMavenFixture
    extends ExternalResource
{

    private ApiFixture api;

    private ArtifactManager artifacts;

    private ArtifactMetadataManager metadata;

    private TypeMapper mapper;

    private MavenPomReader pomReader;

    private MavenPluginDefaults pluginDefaults;

    private MavenPluginImplications pluginImplications;

    private XPathManager xpathManager;

    private XMLInfrastructure xmlInfra;

    private MavenMetadataReader metaReader;

    private VersionResolverImpl versions;

    public GalleyMavenFixture( final ApiFixture api )
    {
        this.api = api;
    }

    public void initMissingComponents()
        throws Exception
    {
        api.initMissingComponents();

        if ( mapper == null )
        {
            mapper = new StandardTypeMapper();
        }

        if ( metadata == null )
        {
            this.metadata = new ArtifactMetadataManagerImpl( api.getTransfers(), api.getLocations() );
        }

        if ( metaReader == null )
        {
            metaReader = new MavenMetadataReader( getXmlInfra(), getLocations(), getMetadata(), getXpathManager() );
        }

        if ( versions == null )
        {
            versions = new VersionResolverImpl( metaReader );
        }

        if ( artifacts == null )
        {
            this.artifacts =
                new ArtifactManagerImpl( api.getTransfers(), api.getLocations(), getMapper(), getVersions() );
        }

        if ( pluginDefaults == null )
        {
            pluginDefaults = new StandardMaven304PluginDefaults();
        }

        if ( xmlInfra == null )
        {
            xmlInfra = new XMLInfrastructure();
        }

        if ( pluginImplications == null )
        {
            pluginImplications = new StandardMavenPluginImplications( xmlInfra );
        }

        if ( xpathManager == null )
        {
            xpathManager = new XPathManager();
        }

        if ( pomReader == null && artifacts != null )
        {
            pomReader =
                new MavenPomReader( xmlInfra, getLocations(), artifacts, xpathManager, pluginDefaults,
                                    pluginImplications );
        }
    }

    @Override
    public void before()
        throws Throwable
    {
        super.before();
        api.before();
    }

    @Override
    public void after()
    {
        api.after();
        super.after();
    }

    public ArtifactManager getArtifacts()
    {
        return artifacts;
    }

    public ArtifactMetadataManager getMetadata()
    {
        return metadata;
    }

    public GalleyMavenFixture setArtifacts( final ArtifactManager artifacts )
    {
        this.artifacts = artifacts;
        return this;
    }

    public GalleyMavenFixture setMetadata( final ArtifactMetadataManager metadata )
    {
        this.metadata = metadata;
        return this;
    }

    public TypeMapper getMapper()
    {
        return mapper;
    }

    public GalleyMavenFixture setMapper( final StandardTypeMapper mapper )
    {
        this.mapper = mapper;
        return this;
    }

    public MavenPomReader getPomReader()
    {
        return pomReader;
    }

    public GalleyMavenFixture setPomReader( final MavenPomReader pomReader )
    {
        this.pomReader = pomReader;
        return this;
    }

    public MavenPluginDefaults getPluginDefaults()
    {
        return pluginDefaults;
    }

    public void setPluginDefaults( final StandardMaven304PluginDefaults pluginDefaults )
    {
        this.pluginDefaults = pluginDefaults;
    }

    public XPathManager getXpathManager()
    {
        return xpathManager;
    }

    public void setXpathManager( final XPathManager xpathManager )
    {
        this.xpathManager = xpathManager;
    }

    public TemporaryFolder getTemp()
    {
        return api.getTemp();
    }

    public LocationExpander getLocations()
    {
        return api.getLocations();
    }

    public TransferDecorator getDecorator()
    {
        return api.getDecorator();
    }

    public FileEventManager getEvents()
    {
        return api.getEvents();
    }

    public TestCacheProvider getCache()
    {
        return api.getCache();
    }

    public TestTransport getTransport()
    {
        return api.getTransport();
    }

    public NotFoundCache getNfc()
    {
        return api.getNfc();
    }

    public ApiFixture setTemp( final TemporaryFolder temp )
    {
        return api.setTemp( temp );
    }

    public ApiFixture setLocations( final LocationExpander locations )
    {
        return api.setLocations( locations );
    }

    public ApiFixture setDecorator( final TransferDecorator decorator )
    {
        return api.setDecorator( decorator );
    }

    public ApiFixture setEvents( final FileEventManager events )
    {
        return api.setEvents( events );
    }

    public ApiFixture setCache( final TestCacheProvider cache )
    {
        return api.setCache( cache );
    }

    public ApiFixture setTransport( final TestTransport transport )
    {
        return api.setTransport( transport );
    }

    public ApiFixture setNfc( final NotFoundCache nfc )
    {
        return api.setNfc( nfc );
    }

    public TransportManager getTransports()
    {
        return api.getTransports();
    }

    public TransferManager getTransfers()
    {
        return api.getTransfers();
    }

    public ApiFixture setTransports( final TransportManager transports )
    {
        return api.setTransports( transports );
    }

    public ApiFixture setTransfers( final TransferManager transfers )
    {
        return api.setTransfers( transfers );
    }

    public XMLInfrastructure getXmlInfra()
    {
        return xmlInfra;
    }

    public void setXmlInfra( final XMLInfrastructure xmlInfra )
    {
        this.xmlInfra = xmlInfra;
    }

    public ApiFixture getApi()
    {
        return api;
    }

    public void setApi( final ApiFixture api )
    {
        this.api = api;
    }

    public void setMapper( final TypeMapper mapper )
    {
        this.mapper = mapper;
    }

    public void setPluginDefaults( final MavenPluginDefaults pluginDefaults )
    {
        this.pluginDefaults = pluginDefaults;
    }

    public MavenPluginImplications getPluginImplications()
    {
        return pluginImplications;
    }

    public void setPluginImplications( final MavenPluginImplications pluginImplications )
    {
        this.pluginImplications = pluginImplications;
    }

    public MavenMetadataReader getMetaReader()
    {
        return metaReader;
    }

    public VersionResolverImpl getVersions()
    {
        return versions;
    }

    public void setMetaReader( final MavenMetadataReader metaReader )
    {
        this.metaReader = metaReader;
    }

    public void setVersions( final VersionResolverImpl versions )
    {
        this.versions = versions;
    }
}
