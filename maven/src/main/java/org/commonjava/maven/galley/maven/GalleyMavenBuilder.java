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
package org.commonjava.maven.galley.maven;

import org.commonjava.maven.galley.GalleyCoreBuilder;
import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.cache.CacheProviderFactory;
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
import org.commonjava.maven.galley.maven.spi.version.VersionResolver;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class GalleyMavenBuilder
{

    private final GalleyCoreBuilder coreBuilder;

    private ArtifactManager artifactManager;

    private ArtifactMetadataManager metadata;

    private TypeMapper mapper;

    private MavenPomReader pomReader;

    private MavenPluginDefaults pluginDefaults;

    private MavenPluginImplications pluginImplications;

    private XPathManager xpathManager;

    private XMLInfrastructure xmlInfra;

    private MavenMetadataReader metaReader;

    private VersionResolver versionResolver;

    public GalleyMavenBuilder()
    {
        this.coreBuilder = new GalleyCoreBuilder();
    }

    public GalleyMavenBuilder( CacheProvider cache )
    {
        this.coreBuilder = new GalleyCoreBuilder().withCache(cache);
    }

    public GalleyMavenBuilder( CacheProviderFactory cacheProviderFactory )
    {
        this.coreBuilder = new GalleyCoreBuilder( cacheProviderFactory );
    }

    public GalleyMaven build()
            throws GalleyInitException
    {
        initMissingComponents();
        return new GalleyMaven( coreBuilder.build(), artifactManager, metadata, mapper, pomReader, pluginDefaults,
                                pluginImplications, xpathManager, xmlInfra, metaReader, versionResolver );
    }

    public void initMissingComponents()
            throws GalleyInitException
    {
        coreBuilder.initMissingComponents();

        if ( mapper == null )
        {
            mapper = new StandardTypeMapper();
        }

        if ( metadata == null )
        {
            this.metadata = new ArtifactMetadataManagerImpl( coreBuilder.getTransferManager(),
                                                             coreBuilder.getLocationExpander() );
        }

        if ( xmlInfra == null )
        {
            xmlInfra = new XMLInfrastructure();
        }

        if ( metaReader == null )
        {
            metaReader = new MavenMetadataReader( xmlInfra, coreBuilder.getLocationExpander(), metadata, xpathManager );
        }

        if ( versionResolver == null )
        {
            versionResolver = new VersionResolverImpl( metaReader );
        }

        if ( artifactManager == null )
        {
            this.artifactManager =
                    new ArtifactManagerImpl( coreBuilder.getTransferManager(), coreBuilder.getLocationExpander(),
                                             mapper, versionResolver );
        }

        if ( pluginDefaults == null )
        {
            pluginDefaults = new StandardMaven304PluginDefaults();
        }

        if ( pluginImplications == null )
        {
            pluginImplications = new StandardMavenPluginImplications( xmlInfra );
        }

        if ( xpathManager == null )
        {
            xpathManager = new XPathManager();
        }

        if ( pomReader == null && artifactManager != null )
        {
            pomReader = new MavenPomReader( xmlInfra, coreBuilder.getLocationExpander(), artifactManager, xpathManager,
                                            pluginDefaults, pluginImplications );
        }
    }

    public ArtifactManager getArtifactManager()
    {
        return artifactManager;
    }

    public ArtifactMetadataManager getArtifactMetadataManager()
    {
        return metadata;
    }

    public GalleyMavenBuilder withArtifactManager( final ArtifactManager artifactManager )
    {
        this.artifactManager = artifactManager;
        return this;
    }

    public GalleyMavenBuilder withArtifactMetadataManager( final ArtifactMetadataManager metadata )
    {
        this.metadata = metadata;
        return this;
    }

    public TypeMapper getTypeMapper()
    {
        return mapper;
    }

    public MavenPomReader getPomReader()
    {
        return pomReader;
    }

    public GalleyMavenBuilder withPomReader( final MavenPomReader pomReader )
    {
        this.pomReader = pomReader;
        return this;
    }

    public MavenPluginDefaults getPluginDefaults()
    {
        return pluginDefaults;
    }

    public GalleyMavenBuilder withPluginDefaults( final StandardMaven304PluginDefaults pluginDefaults )
    {
        this.pluginDefaults = pluginDefaults;
        return this;
    }

    public XPathManager getXPathManager()
    {
        return xpathManager;
    }

    public GalleyMavenBuilder withXPathManager( final XPathManager xpathManager )
    {
        this.xpathManager = xpathManager;
        return this;
    }

    public XMLInfrastructure getXmlInfrastructure()
    {
        return xmlInfra;
    }

    public GalleyMavenBuilder withXmlInfrastructure( final XMLInfrastructure xmlInfra )
    {
        this.xmlInfra = xmlInfra;
        return this;
    }

    public GalleyMavenBuilder withTypeMapper( final TypeMapper mapper )
    {
        this.mapper = mapper;
        return this;
    }

    public GalleyMavenBuilder withPluginDefaults( final MavenPluginDefaults pluginDefaults )
    {
        this.pluginDefaults = pluginDefaults;
        return this;
    }

    public MavenPluginImplications getPluginImplications()
    {
        return pluginImplications;
    }

    public GalleyMavenBuilder withPluginImplications( final MavenPluginImplications pluginImplications )
    {
        this.pluginImplications = pluginImplications;
        return this;
    }

    public MavenMetadataReader getMavenMetadataReader()
    {
        return metaReader;
    }

    public VersionResolver getVersionResolver()
    {
        return versionResolver;
    }

    public GalleyMavenBuilder withMavenMetadataReader( final MavenMetadataReader metaReader )
    {
        this.metaReader = metaReader;
        return this;
    }

    public GalleyMavenBuilder withVersionResolver( final VersionResolver versionResolver )
    {
        this.versionResolver = versionResolver;
        return this;
    }

    public LocationExpander getLocationExpander()
    {
        return coreBuilder.getLocationExpander();
    }

    public LocationResolver getLocationResolver()
    {
        return coreBuilder.getLocationResolver();
    }

    public TransferDecorator getTransferDecorator()
    {
        return coreBuilder.getTransferDecorator();
    }

    public FileEventManager getFileEvents()
    {
        return coreBuilder.getFileEvents();
    }

    public CacheProvider getCache()
    {
        return coreBuilder.getCache();
    }

    public NotFoundCache getNfc()
    {
        return coreBuilder.getNfc();
    }

    public GalleyMavenBuilder withLocationExpander( final LocationExpander locationExpander )
    {
        coreBuilder.withLocationExpander( locationExpander );
        return this;
    }

    public GalleyMavenBuilder withLocationResolver( final LocationResolver locationResolver )
    {
        coreBuilder.withLocationResolver( locationResolver );
        return this;
    }

    public GalleyMavenBuilder withTransferDecorator( final TransferDecorator decorator )
    {
        coreBuilder.withTransferDecorator( decorator );
        return this;
    }

    public GalleyMavenBuilder withFileEvents( final FileEventManager events )
    {
        coreBuilder.withFileEvents( events );
        return this;
    }

    public GalleyMavenBuilder withCache( final CacheProvider cache )
    {
        coreBuilder.withCache( cache );
        return this;
    }

    public GalleyMavenBuilder withNfc( final NotFoundCache nfc )
    {
        coreBuilder.withNfc( nfc );
        return this;
    }

    public TransportManager getTransportManager()
    {
        return coreBuilder.getTransportManager();
    }

    public TransferManager getTransferManager()
    {
        return coreBuilder.getTransferManager();
    }

    public GalleyMavenBuilder withTransportManager( final TransportManager transportManager )
    {
        coreBuilder.withTransportManager( transportManager );
        return this;
    }

    public GalleyMavenBuilder withTransferManager( final TransferManager transferManager )
    {
        coreBuilder.withTransferManager( transferManager );
        return this;
    }

    public List<Transport> getEnabledTransports()
    {
        return coreBuilder.getEnabledTransports();
    }

    public GalleyMavenBuilder withEnabledTransports( final List<Transport> transports )
    {
        coreBuilder.withEnabledTransports( transports );
        return this;
    }

    public GalleyMavenBuilder withEnabledTransports( final Transport... transports )
    {
        coreBuilder.withEnabledTransports( transports );
        return this;
    }

    public ExecutorService getHandlerExecutor()
    {
        return coreBuilder.getHandlerExecutor();
    }

    public GalleyMavenBuilder withHandlerExecutor( final ExecutorService handlerExecutor )
    {
        coreBuilder.withHandlerExecutor( handlerExecutor );
        return this;
    }

    public ExecutorService getBatchExecutor()
    {
        return coreBuilder.getBatchExecutor();
    }

    public GalleyMavenBuilder withBatchExecutor( final ExecutorService batchExecutor )
    {
        coreBuilder.withBatchExecutor( batchExecutor );
        return this;
    }

    public PasswordManager getPasswordManager()
    {
        return coreBuilder.getPasswordManager();
    }

    public GalleyMavenBuilder withPasswordManager( final PasswordManager passwordManager )
    {
        coreBuilder.withPasswordManager( passwordManager );
        return this;
    }

    public GalleyMavenBuilder withAdditionalTransport( final Transport transport )
    {
        coreBuilder.withAdditionalTransport( transport );
        return this;
    }

    public PathGenerator getPathGenerator()
    {
        return coreBuilder.getPathGenerator();
    }

    public GalleyMavenBuilder withPathGenerator( PathGenerator pathGenerator )
    {
        coreBuilder.withPathGenerator( pathGenerator );
        return this;
    }

    public GalleyMavenBuilder withCacheProviderFactory( CacheProviderFactory cacheProviderFactory )
    {
        coreBuilder.withCacheProviderFactory( cacheProviderFactory );
        return this;
    }

    public CacheProviderFactory getCacheProviderFactory()
    {
        return coreBuilder.getCacheProviderFactory();
    }
}
