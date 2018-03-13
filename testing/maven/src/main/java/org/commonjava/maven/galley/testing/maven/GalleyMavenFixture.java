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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.commonjava.maven.galley.GalleyInitException;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.cache.CacheProviderFactory;
import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.cache.FileCacheProviderFactory;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.maven.ArtifactMetadataManager;
import org.commonjava.maven.galley.maven.GalleyMaven;
import org.commonjava.maven.galley.maven.GalleyMavenBuilder;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMaven304PluginDefaults;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMaven350PluginDefaults;
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
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.testing.core.cache.TestCacheProvider;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class GalleyMavenFixture
    extends ExternalResource
{

    private GalleyMavenBuilder mavenBuilder;

    private GalleyMaven maven;

    private TemporaryFolder temp;

    private TestTransport testTransport;

    private Transport[] extraTransports;

    private final boolean autoInit;

    private File cacheDir;

    public GalleyMavenFixture( final TemporaryFolder temp )
    {
        this.autoInit = true;
        this.temp = temp;
    }

    public GalleyMavenFixture( final boolean autoInit, final TemporaryFolder temp )
    {
        this.autoInit = autoInit;
        this.temp = temp;
    }

    public GalleyMavenFixture( final boolean autoInit )
    {
        this.autoInit = autoInit;
    }

    public GalleyMavenFixture()
    {
        this.autoInit = true;
    }

    public void initGalley()
        throws IOException
    {
        if ( temp == null )
        {
            temp = new TemporaryFolder();
        }

        temp.create();
        if ( cacheDir == null )
        {
            cacheDir = temp.newFolder( "cache" );
        }

        mavenBuilder = new GalleyMavenBuilder();
        mavenBuilder.withCacheProviderFactory( new FileCacheProviderFactory( cacheDir ) );
    }

    public GalleyMaven getGalleyMaven()
        throws GalleyInitException
    {
        if ( maven == null )
        {
            maven = mavenBuilder.build();
        }

        return maven;
    }

    public void initTestTransport()
    {
        testTransport = new TestTransport();
        mavenBuilder.withAdditionalTransport( testTransport );
    }

    public void initMissingComponents()
        throws Exception
    {
        if ( mavenBuilder == null )
        {
            initGalley();
        }

        final List<Transport> transports = mavenBuilder.getEnabledTransports();
        if ( transports == null || transports.isEmpty() )
        {
            initTestTransport();
        }

        mavenBuilder.initMissingComponents();
    }

    @Override
    public void before()
        throws Throwable
    {
        if ( autoInit )
        {
            if ( maven == null )
            {
                initMissingComponents();
                if ( mavenBuilder.getCacheProviderFactory() == null && mavenBuilder.getCache() == null && temp != null && cacheDir == null )
                {
                    cacheDir = temp.newFolder( "cache" );
                }

                mavenBuilder.withCache( new FileCacheProvider( cacheDir, mavenBuilder.getPathGenerator(),
                                                               mavenBuilder.getFileEvents(),
                                                               mavenBuilder.getTransferDecorator() ) );
                maven = mavenBuilder.build();
            }
        }

        super.before();
    }

    @Override
    public void after()
    {
        maven = null;
        super.after();
    }

    private void checkInitialized()
    {
        if ( maven != null )
        {
            throw new IllegalStateException( "Already initialized!" );
        }
    }

    public Transport[] getExtraTransports()
    {
        return extraTransports;
    }

    public GalleyMavenFixture withExtraTransports( final Transport... transports )
    {
        checkInitialized();
        this.extraTransports = transports;
        for ( final Transport transport : transports )
        {
            mavenBuilder.withAdditionalTransport( transport );
        }
        return this;
    }

    public TestTransport getTransport()
    {
        return testTransport;
    }

    public NotFoundCache getNfc()
    {
        return maven == null ? mavenBuilder.getNfc() : mavenBuilder.getNfc();
    }

    public TemporaryFolder getTemp()
    {
        return temp;
    }

    public GalleyMavenFixture withTemp( final TemporaryFolder temp )
    {
        if ( this.temp != null )
        {
            this.temp.delete();
        }
        this.temp = temp;
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setTemp( final TemporaryFolder temp )
    {
        if ( this.temp != null )
        {
            this.temp.delete();
        }
        this.temp = temp;
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setExtraTransports( final Transport... transports )
    {
        checkInitialized();
        this.extraTransports = transports;
        for ( final Transport transport : transports )
        {
            mavenBuilder.withAdditionalTransport( transport );
        }
        return this;
    }

    @Deprecated
    public ArtifactManager getArtifacts()
    {
        return maven == null ? mavenBuilder.getArtifactManager() : mavenBuilder.getArtifactManager();
    }

    @Deprecated
    public ArtifactMetadataManager getMetadata()
    {
        return maven == null ? mavenBuilder.getArtifactMetadataManager() : mavenBuilder.getArtifactMetadataManager();
    }

    @Deprecated
    public GalleyMavenFixture setArtifacts( final ArtifactManager artifacts )
    {
        checkInitialized();
        mavenBuilder.withArtifactManager( artifacts );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setMetadata( final ArtifactMetadataManager metadata )
    {
        checkInitialized();
        mavenBuilder.withArtifactMetadataManager( metadata );
        return this;
    }

    @Deprecated
    public TypeMapper getMapper()
    {
        return maven == null ? mavenBuilder.getTypeMapper() : maven.getTypeMapper();
    }

    @Deprecated
    public GalleyMavenFixture setMapper( final StandardTypeMapper mapper )
    {
        checkInitialized();
        mavenBuilder.withTypeMapper( mapper );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setPomReader( final MavenPomReader pomReader )
    {
        checkInitialized();
        mavenBuilder.withPomReader( pomReader );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setXpathManager( final XPathManager xpathManager )
    {
        checkInitialized();
        mavenBuilder.withXPathManager( xpathManager );
        return this;
    }

    @Deprecated
    public LocationExpander getLocations()
    {
        return maven == null ? mavenBuilder.getLocationExpander() : mavenBuilder.getLocationExpander();
    }

    @Deprecated
    public TransferDecorator getDecorator()
    {
        return maven == null ? mavenBuilder.getTransferDecorator() : mavenBuilder.getTransferDecorator();
    }

    @Deprecated
    public FileEventManager getEvents()
    {
        return maven == null ? mavenBuilder.getFileEvents() : mavenBuilder.getFileEvents();
    }

    @Deprecated
    public GalleyMavenFixture setLocations( final LocationExpander locations )
    {
        checkInitialized();
        mavenBuilder.withLocationExpander( locations );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setDecorator( final TransferDecorator decorator )
    {
        checkInitialized();
        mavenBuilder.withTransferDecorator( decorator );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setEvents( final FileEventManager events )
    {
        checkInitialized();
        mavenBuilder.withFileEvents( events );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setCache( final TestCacheProvider cache )
    {
        checkInitialized();
        mavenBuilder.withCache( cache );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setTransport( final TestTransport transport )
    {
        this.testTransport = transport;
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setNfc( final NotFoundCache nfc )
    {
        checkInitialized();
        mavenBuilder.withNfc( nfc );
        return this;
    }

    @Deprecated
    public TransportManager getTransports()
    {
        return maven == null ? mavenBuilder.getTransportManager() : mavenBuilder.getTransportManager();
    }

    @Deprecated
    public TransferManager getTransfers()
    {
        return maven == null ? mavenBuilder.getTransferManager() : mavenBuilder.getTransferManager();
    }

    @Deprecated
    public GalleyMavenFixture setTransports( final TransportManager transports )
    {
        checkInitialized();
        mavenBuilder.withTransportManager( transports );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setTransfers( final TransferManager transfers )
    {
        checkInitialized();
        mavenBuilder.withTransferManager( transfers );
        return this;
    }

    @Deprecated
    public XMLInfrastructure getXmlInfra()
    {
        return maven == null ? mavenBuilder.getXmlInfrastructure() : mavenBuilder.getXmlInfrastructure();
    }

    @Deprecated
    public GalleyMavenFixture setXmlInfra( final XMLInfrastructure xmlInfra )
    {
        checkInitialized();
        mavenBuilder.withXmlInfrastructure( xmlInfra );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setMapper( final TypeMapper mapper )
    {
        checkInitialized();
        mavenBuilder.withTypeMapper( mapper );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setPluginDefaults( final MavenPluginDefaults pluginDefaults )
    {
        checkInitialized();
        mavenBuilder.withPluginDefaults( pluginDefaults );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setPluginImplications( final MavenPluginImplications pluginImplications )
    {
        checkInitialized();
        mavenBuilder.withPluginImplications( pluginImplications );
        return this;
    }

    @Deprecated
    public MavenMetadataReader getMetaReader()
    {
        return maven == null ? mavenBuilder.getMavenMetadataReader() : mavenBuilder.getMavenMetadataReader();
    }

    @Deprecated
    public VersionResolver getVersions()
    {
        return maven == null ? mavenBuilder.getVersionResolver() : mavenBuilder.getVersionResolver();
    }

    @Deprecated
    public GalleyMavenFixture setMetaReader( final MavenMetadataReader metaReader )
    {
        checkInitialized();
        mavenBuilder.withMavenMetadataReader( metaReader );
        return this;
    }

    @Deprecated
    public GalleyMavenFixture setVersions( final VersionResolverImpl versions )
    {
        checkInitialized();
        mavenBuilder.withVersionResolver( versions );
        return this;
    }

    public ArtifactManager getArtifactManager()
    {
        return maven == null ? mavenBuilder.getArtifactManager() : maven.getArtifactManager();
    }

    public ArtifactMetadataManager getArtifactMetadataManager()
    {
        return maven == null ? mavenBuilder.getArtifactMetadataManager() : maven.getArtifactMetadataManager();
    }

    public TypeMapper getTypeMapper()
    {
        return maven == null ? mavenBuilder.getTypeMapper() : maven.getTypeMapper();
    }

    public MavenPomReader getPomReader()
    {
        return maven == null ? mavenBuilder.getPomReader() : maven.getPomReader();
    }

    public MavenPluginDefaults getPluginDefaults()
    {
        return maven == null ? mavenBuilder.getPluginDefaults() : maven.getPluginDefaults();
    }

    public XPathManager getXPathManager()
    {
        return maven == null ? mavenBuilder.getXPathManager() : maven.getXPathManager();
    }

    public XMLInfrastructure getXmlInfrastructure()
    {
        return maven == null ? mavenBuilder.getXmlInfrastructure() : maven.getXmlInfrastructure();
    }

    public MavenPluginImplications getPluginImplications()
    {
        return maven == null ? mavenBuilder.getPluginImplications() : maven.getPluginImplications();
    }

    public MavenMetadataReader getMavenMetadataReader()
    {
        return maven == null ? mavenBuilder.getMavenMetadataReader() : maven.getMavenMetadataReader();
    }

    public VersionResolver getVersionResolver()
    {
        return maven == null ? mavenBuilder.getVersionResolver() : maven.getVersionResolver();
    }

    public LocationExpander getLocationExpander()
    {
        return maven == null ? mavenBuilder.getLocationExpander() : maven.getLocationExpander();
    }

    public LocationResolver getLocationResolver()
    {
        return maven == null ? mavenBuilder.getLocationResolver() : maven.getLocationResolver();
    }

    public TransferDecorator getTransferDecorator()
    {
        return maven == null ? mavenBuilder.getTransferDecorator() : maven.getTransferDecorator();
    }

    public FileEventManager getFileEvents()
    {
        return maven == null ? mavenBuilder.getFileEvents() : maven.getFileEvents();
    }

    public CacheProvider getCache()
    {
        return maven == null ? mavenBuilder.getCache() : maven.getCache();
    }

    public TransportManager getTransportManager()
    {
        return maven == null ? mavenBuilder.getTransportManager() : maven.getTransportManager();
    }

    public TransferManager getTransferManager()
    {
        return maven == null ? mavenBuilder.getTransferManager() : maven.getTransferManager();
    }

    public List<Transport> getEnabledTransports()
    {
        return maven == null ? mavenBuilder.getEnabledTransports() : maven.getEnabledTransports();
    }

    public ExecutorService getHandlerExecutor()
    {
        return maven == null ? mavenBuilder.getHandlerExecutor() : maven.getHandlerExecutor();
    }

    public ExecutorService getBatchExecutor()
    {
        return maven == null ? mavenBuilder.getBatchExecutor() : maven.getBatchExecutor();
    }

    public PasswordManager getPasswordManager()
    {
        return maven == null ? mavenBuilder.getPasswordManager() : maven.getPasswordManager();
    }

    public GalleyMavenFixture withArtifactManager( final ArtifactManager artifactManager )
    {
        checkInitialized();
        mavenBuilder.withArtifactManager( artifactManager );
        return this;
    }

    public GalleyMavenFixture withArtifactMetadataManager( final ArtifactMetadataManager metadata )
    {
        checkInitialized();
        mavenBuilder.withArtifactMetadataManager( metadata );
        return this;
    }

    public GalleyMavenFixture withPomReader( final MavenPomReader pomReader )
    {
        checkInitialized();
        mavenBuilder.withPomReader( pomReader );
        return this;
    }

    public GalleyMavenFixture withPluginDefaults( final StandardMaven350PluginDefaults pluginDefaults )
    {
        checkInitialized();
        mavenBuilder.withPluginDefaults( pluginDefaults );
        return this;
    }

    public GalleyMavenFixture withXPathManager( final XPathManager xpathManager )
    {
        checkInitialized();
        mavenBuilder.withXPathManager( xpathManager );
        return this;
    }

    public GalleyMavenFixture withXmlInfrastructure( final XMLInfrastructure xmlInfra )
    {
        checkInitialized();
        mavenBuilder.withXmlInfrastructure( xmlInfra );
        return this;
    }

    public GalleyMavenFixture withTypeMapper( final TypeMapper mapper )
    {
        checkInitialized();
        mavenBuilder.withTypeMapper( mapper );
        return this;
    }

    public GalleyMavenFixture withPluginDefaults( final MavenPluginDefaults pluginDefaults )
    {
        checkInitialized();
        mavenBuilder.withPluginDefaults( pluginDefaults );
        return this;
    }

    public GalleyMavenFixture withPluginImplications( final MavenPluginImplications pluginImplications )
    {
        checkInitialized();
        mavenBuilder.withPluginImplications( pluginImplications );
        return this;
    }

    public GalleyMavenFixture withMavenMetadataReader( final MavenMetadataReader metaReader )
    {
        checkInitialized();
        mavenBuilder.withMavenMetadataReader( metaReader );
        return this;
    }

    public GalleyMavenFixture withVersionResolver( final VersionResolver versionResolver )
    {
        checkInitialized();
        mavenBuilder.withVersionResolver( versionResolver );
        return this;
    }

    public GalleyMavenFixture withLocationExpander( final LocationExpander locationExpander )
    {
        checkInitialized();
        mavenBuilder.withLocationExpander( locationExpander );
        return this;
    }

    public GalleyMavenFixture withLocationResolver( final LocationResolver locationResolver )
    {
        checkInitialized();
        mavenBuilder.withLocationResolver( locationResolver );
        return this;
    }

    public GalleyMavenFixture withTransferDecorator( final TransferDecorator decorator )
    {
        checkInitialized();
        mavenBuilder.withTransferDecorator( decorator );
        return this;
    }

    public GalleyMavenFixture withFileEvents( final FileEventManager events )
    {
        checkInitialized();
        mavenBuilder.withFileEvents( events );
        return this;
    }

    public GalleyMavenFixture withCache( final CacheProvider cache )
    {
        checkInitialized();
        mavenBuilder.withCache( cache );
        return this;
    }

    public GalleyMavenFixture withNfc( final NotFoundCache nfc )
    {
        checkInitialized();
        mavenBuilder.withNfc( nfc );
        return this;
    }

    public GalleyMavenFixture withTransportManager( final TransportManager transportManager )
    {
        checkInitialized();
        mavenBuilder.withTransportManager( transportManager );
        return this;
    }

    public GalleyMavenFixture withTransferManager( final TransferManager transferManager )
    {
        checkInitialized();
        mavenBuilder.withTransferManager( transferManager );
        return this;
    }

    public GalleyMavenFixture withEnabledTransports( final List<Transport> transports )
    {
        checkInitialized();
        mavenBuilder.withEnabledTransports( transports );
        return this;
    }

    public GalleyMavenFixture withEnabledTransports( final Transport... transports )
    {
        checkInitialized();
        mavenBuilder.withEnabledTransports( transports );
        return this;
    }

    public GalleyMavenFixture withHandlerExecutor( final ExecutorService handlerExecutor )
    {
        checkInitialized();
        mavenBuilder.withHandlerExecutor( handlerExecutor );
        return this;
    }

    public GalleyMavenFixture withBatchExecutor( final ExecutorService batchExecutor )
    {
        checkInitialized();
        mavenBuilder.withBatchExecutor( batchExecutor );
        return this;
    }

    public GalleyMavenFixture withPasswordManager( final PasswordManager passwordManager )
    {
        checkInitialized();
        mavenBuilder.withPasswordManager( passwordManager );
        return this;
    }

    public GalleyMavenFixture withAdditionalTransport( final Transport transport )
    {
        checkInitialized();
        mavenBuilder.withAdditionalTransport( transport );
        return this;
    }

    public GalleyMavenFixture withCacheProviderFactory( CacheProviderFactory cacheProviderFactory )
    {
        mavenBuilder.withCacheProviderFactory( cacheProviderFactory );
        return this;
    }

    public CacheProviderFactory getCacheProviderFactory()
    {
        return mavenBuilder.getCacheProviderFactory();
    }

    @Deprecated
    public XPathManager getXpathManager()
    {
        return getXPathManager();
    }

}
