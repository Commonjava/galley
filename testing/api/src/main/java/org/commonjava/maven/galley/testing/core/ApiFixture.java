package org.commonjava.maven.galley.testing.core;

import org.commonjava.maven.galley.ArtifactManager;
import org.commonjava.maven.galley.ArtifactMetadataManager;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.maven.defaults.StandardMaven304PluginDefaults;
import org.commonjava.maven.galley.maven.reader.MavenPomReader;
import org.commonjava.maven.galley.maven.view.XPathManager;
import org.commonjava.maven.galley.nfc.NoOpNotFoundCache;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.testing.core.cache.TestCacheProvider;
import org.commonjava.maven.galley.testing.core.transport.TestTransport;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.maven.galley.type.StandardTypeMapper;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class ApiFixture
    extends ExternalResource
{

    private TemporaryFolder temp;

    private LocationExpander locations;

    private TransferDecorator decorator;

    private FileEventManager events;

    private TestCacheProvider cache;

    private TestTransport transport;

    private NotFoundCache nfc;

    private TransportManager transports;

    private TransferManager transfers;

    private ArtifactManager artifacts;

    private ArtifactMetadataManager metadata;

    private StandardTypeMapper mapper;

    private MavenPomReader pomReader;

    private StandardMaven304PluginDefaults pluginDefaults;

    private XPathManager xpathManager;

    public ApiFixture()
    {
        temp = new TemporaryFolder();
    }

    public ApiFixture( final TemporaryFolder temp )
    {
        this.temp = temp;
    }

    public void initMissingComponents()
    {
        if ( locations == null )
        {
            locations = new NoOpLocationExpander();
        }

        if ( mapper == null )
        {
            mapper = new StandardTypeMapper();
        }

        if ( transport == null )
        {
            transport = new TestTransport( mapper );
        }

        if ( decorator == null )
        {
            decorator = new NoOpTransferDecorator();
        }
        if ( nfc == null )
        {
            nfc = new NoOpNotFoundCache();
        }
        if ( events == null )
        {
            events = new NoOpFileEventManager();
        }
        if ( cache == null )
        {
            cache = new TestCacheProvider( temp.newFolder( "cache" ), events, decorator );
        }

        if ( pluginDefaults == null )
        {
            pluginDefaults = new StandardMaven304PluginDefaults();
        }

        if ( xpathManager == null )
        {
            xpathManager = new XPathManager();
        }

        if ( pomReader == null && artifacts != null )
        {
            pomReader = new MavenPomReader( artifacts, xpathManager, pluginDefaults );
        }
    }

    @Override
    protected void before()
        throws Throwable
    {
        super.before();
        temp.create();
    }

    @Override
    protected void after()
    {
        temp.delete();
        super.after();
    }

    public TemporaryFolder getTemp()
    {
        return temp;
    }

    public LocationExpander getLocations()
    {
        return locations;
    }

    public TransferDecorator getDecorator()
    {
        return decorator;
    }

    public FileEventManager getEvents()
    {
        return events;
    }

    public TestCacheProvider getCache()
    {
        return cache;
    }

    public TestTransport getTransport()
    {
        return transport;
    }

    public NotFoundCache getNfc()
    {
        return nfc;
    }

    public ApiFixture setTemp( final TemporaryFolder temp )
    {
        this.temp = temp;
        return this;
    }

    public ApiFixture setLocations( final LocationExpander locations )
    {
        this.locations = locations;
        return this;
    }

    public ApiFixture setDecorator( final TransferDecorator decorator )
    {
        this.decorator = decorator;
        return this;
    }

    public ApiFixture setEvents( final FileEventManager events )
    {
        this.events = events;
        return this;
    }

    public ApiFixture setCache( final TestCacheProvider cache )
    {
        this.cache = cache;
        return this;
    }

    public ApiFixture setTransport( final TestTransport transport )
    {
        this.transport = transport;
        return this;
    }

    public ApiFixture setNfc( final NotFoundCache nfc )
    {
        this.nfc = nfc;
        return this;
    }

    public TransportManager getTransports()
    {
        return transports;
    }

    public TransferManager getTransfers()
    {
        return transfers;
    }

    public ArtifactManager getArtifacts()
    {
        return artifacts;
    }

    public ArtifactMetadataManager getMetadata()
    {
        return metadata;
    }

    public ApiFixture setTransports( final TransportManager transports )
    {
        this.transports = transports;
        return this;
    }

    public ApiFixture setTransfers( final TransferManager transfers )
    {
        this.transfers = transfers;
        return this;
    }

    public ApiFixture setArtifacts( final ArtifactManager artifacts )
    {
        this.artifacts = artifacts;
        return this;
    }

    public ApiFixture setMetadata( final ArtifactMetadataManager metadata )
    {
        this.metadata = metadata;
        return this;
    }

    public StandardTypeMapper getMapper()
    {
        return mapper;
    }

    public ApiFixture setMapper( final StandardTypeMapper mapper )
    {
        this.mapper = mapper;
        return this;
    }

    public MavenPomReader getPomReader()
    {
        return pomReader;
    }

    public ApiFixture setPomReader( final MavenPomReader pomReader )
    {
        this.pomReader = pomReader;
        return this;
    }

    public StandardMaven304PluginDefaults getPluginDefaults()
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

}
