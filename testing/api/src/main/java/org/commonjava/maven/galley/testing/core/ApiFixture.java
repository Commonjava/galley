package org.commonjava.maven.galley.testing.core;

import org.commonjava.maven.galley.ArtifactManager;
import org.commonjava.maven.galley.ArtifactMetadataManager;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
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
        if ( transport == null )
        {
            transport = new TestTransport();
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

        if ( mapper == null )
        {
            mapper = new StandardTypeMapper();
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

    public void setTemp( final TemporaryFolder temp )
    {
        this.temp = temp;
    }

    public void setLocations( final LocationExpander locations )
    {
        this.locations = locations;
    }

    public void setDecorator( final TransferDecorator decorator )
    {
        this.decorator = decorator;
    }

    public void setEvents( final FileEventManager events )
    {
        this.events = events;
    }

    public void setCache( final TestCacheProvider cache )
    {
        this.cache = cache;
    }

    public void setTransport( final TestTransport transport )
    {
        this.transport = transport;
    }

    public void setNfc( final NotFoundCache nfc )
    {
        this.nfc = nfc;
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

    public void setTransports( final TransportManager transports )
    {
        this.transports = transports;
    }

    public void setTransfers( final TransferManager transfers )
    {
        this.transfers = transfers;
    }

    public void setArtifacts( final ArtifactManager artifacts )
    {
        this.artifacts = artifacts;
    }

    public void setMetadata( final ArtifactMetadataManager metadata )
    {
        this.metadata = metadata;
    }

    public StandardTypeMapper getMapper()
    {
        return mapper;
    }

    public void setMapper( final StandardTypeMapper mapper )
    {
        this.mapper = mapper;
    }

}
