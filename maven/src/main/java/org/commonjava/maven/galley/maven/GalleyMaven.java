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
package org.commonjava.maven.galley.maven;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.commonjava.maven.galley.GalleyCore;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
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
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;

public class GalleyMaven
{

    @Inject
    private GalleyCore core;

    @Inject
    private ArtifactManager artifactManager;

    @Inject
    private ArtifactMetadataManager metadataManager;

    @Inject
    private TypeMapper mapper;

    @Inject
    private MavenPomReader pomReader;

    @Inject
    private MavenPluginDefaults pluginDefaults;

    @Inject
    private MavenPluginImplications pluginImplications;

    @Inject
    private XPathManager xpathManager;

    @Inject
    private XMLInfrastructure xmlInfra;

    @Inject
    private MavenMetadataReader metaReader;

    @Inject
    private VersionResolver versionResolver;

    @SuppressWarnings( "unused" )
    protected GalleyMaven()
    {
    }

    public GalleyMaven( final GalleyCore core, final ArtifactManager artifactManager,
                        final ArtifactMetadataManager metadataManager,
                        final TypeMapper mapper, final MavenPomReader pomReader,
                        final MavenPluginDefaults pluginDefaults, final MavenPluginImplications pluginImplications,
                        final XPathManager xpathManager, final XMLInfrastructure xmlInfra,
                        final MavenMetadataReader metaReader, final VersionResolver versionResolver )
    {
        this.core = core;
        this.artifactManager = artifactManager;
        this.metadataManager = metadataManager;
        this.mapper = mapper;
        this.pomReader = pomReader;
        this.pluginDefaults = pluginDefaults;
        this.pluginImplications = pluginImplications;
        this.xpathManager = xpathManager;
        this.xmlInfra = xmlInfra;
        this.metaReader = metaReader;
        this.versionResolver = versionResolver;
    }

    public ArtifactManager getArtifactManager()
    {
        return artifactManager;
    }

    public ArtifactMetadataManager getArtifactMetadataManager()
    {
        return metadataManager;
    }

    public TypeMapper getTypeMapper()
    {
        return mapper;
    }

    public MavenPomReader getPomReader()
    {
        return pomReader;
    }

    public MavenPluginDefaults getPluginDefaults()
    {
        return pluginDefaults;
    }

    public XPathManager getXPathManager()
    {
        return xpathManager;
    }

    public XMLInfrastructure getXmlInfrastructure()
    {
        return xmlInfra;
    }

    public MavenPluginImplications getPluginImplications()
    {
        return pluginImplications;
    }

    public MavenMetadataReader getMavenMetadataReader()
    {
        return metaReader;
    }

    public VersionResolver getVersionResolver()
    {
        return versionResolver;
    }

    public LocationExpander getLocationExpander()
    {
        return core.getLocationExpander();
    }

    public LocationResolver getLocationResolver()
    {
        return core.getLocationResolver();
    }

    public TransferDecoratorManager getTransferDecorator()
    {
        return core.getTransferDecorator();
    }

    public FileEventManager getFileEvents()
    {
        return core.getFileEvents();
    }

    public CacheProvider getCache()
    {
        return core.getCache();
    }

    public NotFoundCache getNfc()
    {
        return core.getNfc();
    }

    public TransportManager getTransportManager()
    {
        return core.getTransportManager();
    }

    public TransferManager getTransferManager()
    {
        return core.getTransferManager();
    }

    public List<Transport> getEnabledTransports()
    {
        return core.getEnabledTransports();
    }

    public ExecutorService getHandlerExecutor()
    {
        return core.getHandlerExecutor();
    }

    public ExecutorService getBatchExecutor()
    {
        return core.getBatchExecutor();
    }

    public PasswordManager getPasswordManager()
    {
        return core.getPasswordManager();
    }

}
