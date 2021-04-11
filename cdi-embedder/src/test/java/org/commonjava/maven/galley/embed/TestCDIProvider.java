/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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

import io.opentelemetry.api.trace.Tracer;
import org.commonjava.cdi.util.weft.config.DefaultWeftConfig;
import org.commonjava.cdi.util.weft.config.WeftConfig;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.config.TransportMetricConfig;
import org.commonjava.maven.galley.filearc.FileTransportConfig;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.maven.galley.transport.SimpleUrlLocationResolver;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.commonjava.o11yphant.metrics.DefaultTrafficClassifier;
import org.commonjava.o11yphant.metrics.conf.DefaultMetricsConfig;
import org.commonjava.o11yphant.metrics.conf.MetricsConfig;
import org.commonjava.o11yphant.metrics.sli.GoldenSignalsMetricSet;
import org.commonjava.o11yphant.metrics.system.StoragePathProvider;
import org.commonjava.o11yphant.otel.OtelConfiguration;
import org.commonjava.o11yphant.otel.OtelTracePlugin;
import org.commonjava.o11yphant.trace.SpanFieldsDecorator;
import org.commonjava.o11yphant.trace.TraceManager;
import org.commonjava.o11yphant.trace.TracerConfiguration;
import org.commonjava.util.partyline.JoinableFileManager;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

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

    //private MetricRegistry metricRegistry;

    private TransportMetricConfig transportMetricConfig = new TransportMetricConfig()
    {
        public boolean isEnabled() {
            return false;
        }

        @Override
        public String getNodePrefix()
        {
            return null;
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
    private TransferDecoratorManager transferDecorator;

    @Inject
    private TransportManager transportManager;


    @PostConstruct
    public void start()
    {
        try
        {
            temp.create();
            cacheProvider =
                    new PartyLineCacheProvider( temp.newFolder(), pathGenerator, eventManager, transferDecorator,
                                                Executors.newScheduledThreadPool( 2 ), new JoinableFileManager() );
            fileTransportConfig = new FileTransportConfig( temp.newFolder(), pathGenerator );
        }
        catch ( IOException e )
        {
            Assert.fail( "Failed to init temp folder fro file cache." );
        }

        locationExpander = new NoOpLocationExpander();
        locationResolver = new SimpleUrlLocationResolver( locationExpander, transportManager );
        globalHttpConfiguration = new GlobalHttpConfiguration();

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
    public TransportMetricConfig getTransportMetricConfig()
    {
        return transportMetricConfig;
    }

    @Produces
    @Default
    public MetricsConfig getMetricsConfig()
    {
        return new DefaultMetricsConfig();
    }

    @Produces
    @Default
    public StoragePathProvider getStoragePathProvider()
    {
        return () -> null;
    }

    @Produces
    @Default
    public TraceManager getTraceManager()
    {
        OtelConfiguration otelConf = new OtelConfiguration()
        {
        };

        TracerConfiguration traceConf = new TracerConfiguration()
        {
            @Override
            public boolean isEnabled()
            {
                return false;
            }

            @Override
            public String getServiceName()
            {
                return "galley";
            }

            @Override
            public String getNodeId()
            {
                return "node";
            }
        };

        return new TraceManager( new OtelTracePlugin( traceConf, otelConf ), new SpanFieldsDecorator( new ArrayList<>() ), getTracerConfiguration() );
    }

    @Produces
    @Default
    public TracerConfiguration getTracerConfiguration()
    {
        return new TracerConfiguration()
        {
            @Override
            public Map<String, Integer> getSpanRates()
            {
                return null;
            }

            @Override
            public boolean isEnabled()
            {
                return false;
            }

            @Override
            public String getServiceName()
            {
                return null;
            }

            @Override
            public Integer getBaseSampleRate()
            {
                return null;
            }

            @Override
            public Set<String> getFieldSet()
            {
                return null;
            }

            @Override
            public String getEnvironmentMappings()
            {
                return null;
            }

            @Override
            public String getCPNames()
            {
                return null;
            }

            @Override
            public String getNodeId()
            {
                return null;
            }
        };
    }

    @Produces
    @Default
    public DefaultTrafficClassifier getTrafficClassifier()
    {
        return new DefaultTrafficClassifier()
        {
            @Override
            protected List<String> calculateCachedFunctionClassifiers( String restPath, String method, Map<String, String> headers )
            {
                return Collections.emptyList();
            }
        };
    }

    @Produces
    public GoldenSignalsMetricSet getGoldenSignalsMetricSet()
    {
        return null;
    }
}
