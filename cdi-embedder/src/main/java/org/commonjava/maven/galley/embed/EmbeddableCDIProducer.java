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
package org.commonjava.maven.galley.embed;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.maven.atlas.ident.jackson.ProjectVersionRefSerializerModule;
import org.commonjava.maven.galley.auth.MemoryPasswordManager;
import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.cache.FileCacheProviderConfig;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMaven304PluginDefaults;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMavenPluginImplications;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.maven.galley.transport.SimpleUrlLocationResolver;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.HttpImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Created by jdcasey on 9/14/15.
 */
@ApplicationScoped
public class EmbeddableCDIProducer
{

    @Inject
    private FileCacheProviderConfig cacheConfig;

    @Inject
    private TransportManager transportManager;

    @Inject
    private XMLInfrastructure xml;

    private NotFoundCache nfc;

    private LocationExpander locationExpander;

    private LocationResolver locationResolver;

    private ObjectMapper objectMapper;

    private Http http;

    private CacheProvider cacheProvider;

    private FileEventManager fileEventManager;

    private TransferDecorator transferDecorator;

    private PathGenerator pathGenerator;

    private PasswordManager passwordManager;

    private ArtifactManager artifactManager;

    private MavenPluginDefaults pluginDefaults;

    private MavenPluginImplications pluginImplications;

    @PostConstruct
    public void postConstruct()
    {
        fileEventManager = new NoOpFileEventManager();
        transferDecorator = new NoOpTransferDecorator();
        pathGenerator = new HashedLocationPathGenerator();
        cacheProvider = new FileCacheProvider( cacheConfig, pathGenerator, fileEventManager, transferDecorator );
        nfc = new MemoryNotFoundCache();
        locationExpander = new NoOpLocationExpander();
        locationResolver = new SimpleUrlLocationResolver( locationExpander, transportManager );

        passwordManager = new MemoryPasswordManager();
        http = new HttpImpl( passwordManager );

        objectMapper = new ObjectMapper();
        objectMapper.registerModules( new ProjectVersionRefSerializerModule() );

        pluginDefaults = new StandardMaven304PluginDefaults();
        pluginImplications = new StandardMavenPluginImplications( xml );
    }

    @Default
    @Produces
    public MavenPluginImplications getPluginImplications()
    {
        return pluginImplications;
    }

    @Default
    @Produces
    public MavenPluginDefaults getPluginDefaults()
    {
        return pluginDefaults;
    }

    @Default
    @Produces
    public PasswordManager getPasswordManager()
    {
        return passwordManager;
    }

    @Default
    @Produces
    public PathGenerator getPathGenerator()
    {
        return pathGenerator;
    }

    @Default
    @Produces
    public TransferDecorator getTransferDecorator()
    {
        return transferDecorator;
    }

    @Default
    @Produces
    public FileEventManager getFileEventManager()
    {
        return fileEventManager;
    }

    @Default
    @Produces
    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    @Default
    @Produces
    public LocationResolver getLocationResolver()
    {
        return locationResolver;
    }

    @Default
    @Produces
    public CacheProvider getCacheProvider()
    {
        return cacheProvider;
    }

    @Default
    @Produces
    public LocationExpander getLocationExpander()
    {
        return locationExpander;
    }

    @Default
    @Produces
    public NotFoundCache getNotFoundCache()
    {
        return nfc;
    }

    @Default
    @Produces
    public Http getHttp()
    {
        return http;
    }


}
