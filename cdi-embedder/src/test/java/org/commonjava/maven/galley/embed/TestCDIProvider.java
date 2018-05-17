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

import com.codahale.metrics.MetricRegistry;
import org.commonjava.cdi.util.weft.config.DefaultWeftConfig;
import org.commonjava.cdi.util.weft.config.WeftConfig;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.config.TransportMetricConfig;
import org.commonjava.maven.galley.filearc.FileTransportConfig;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.maven.galley.transport.SimpleUrlLocationResolver;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by jdcasey on 9/14/15.
 */
@ApplicationScoped
public class TestCDIProvider
{
    private TemporaryFolder temp = new TemporaryFolder();

    private LocationExpander locationExpander;

    private LocationResolver locationResolver;

    private PartyLineCacheProvider cacheProvider;

    private FileTransportConfig fileTransportConfig;

    private GlobalHttpConfiguration globalHttpConfiguration;

    private WeftConfig weftConfig;

    private MetricRegistry metricRegistry;

    private TransportMetricConfig transportMetricConfig = new TransportMetricConfig()
    {
        public boolean isEnabled() {
            return false;
        }

        @Override
        public String getMetricUniqueName( Location location )
        {
            return null;
        }
    };

    @Inject
    private PathGenerator pathGenerator;

    @Inject
    private FileEventManager eventManager;

    @Inject
    private TransferDecorator transferDecorator;

    @Inject
    private TransportManager transportManager;


    @PostConstruct
    public void start()
    {
        try
        {
            temp.create();
            cacheProvider = new PartyLineCacheProvider( temp.newFolder(), pathGenerator, eventManager, transferDecorator );
            fileTransportConfig = new FileTransportConfig( temp.newFolder(), pathGenerator );
        }
        catch ( IOException e )
        {
            Assert.fail( "Failed to init temp folder fro file cache." );
        }

        locationExpander = new NoOpLocationExpander();
        locationResolver = new SimpleUrlLocationResolver( locationExpander, transportManager );
        globalHttpConfiguration = new GlobalHttpConfiguration();

        metricRegistry = new MetricRegistry();

        weftConfig = new DefaultWeftConfig(  );
    }

    @PreDestroy
    public void stop()
    {
        temp.delete();
    }

    @Produces
    @Default
    public PartyLineCacheProvider getCacheProvider()
    {
        return cacheProvider;
    }

    @Produces
    @Default
    public LocationExpander getLocationExpander()
    {
        return locationExpander;
    }

    @Produces
    @Default
    public LocationResolver getLocationResolver()
    {
        return locationResolver;
    }

    @Produces
    @Default
    public FileTransportConfig getFileTransportConfig()
    {
        return fileTransportConfig;
    }

    @Produces
    @Default
    public GlobalHttpConfiguration getGlobalHttpConfiguration()
    {
        return globalHttpConfiguration;
    }

    @Produces
    @Default
    public WeftConfig getWeftConfig()
    {
        return weftConfig;
    }

    @Produces
    @Default
    public MetricRegistry getMetricRegistry()
    {
        return metricRegistry;
    }

    @Produces
    @Default
    public TransportMetricConfig getTransportMetricConfig()
    {
        return transportMetricConfig;
    }
}
