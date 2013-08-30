package org.commonjava.maven.galley.testing.core.cdi;

import java.util.concurrent.ExecutorService;

import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.nfc.NoOpNotFoundCache;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.testing.core.cache.TestCacheProvider;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.util.cdi.ConfigurationExtension;
import org.commonjava.util.cdi.DefaultInstanceBean;
import org.commonjava.util.cdi.ExecutorServiceBean;
import org.commonjava.util.cdi.ExternalBean;
import org.commonjava.util.cdi.ExternalContext;
import org.junit.rules.TemporaryFolder;

public class ApiCDIExtension
    extends ConfigurationExtension
{

    private NoOpFileEventManager events;

    private NoOpTransferDecorator decorator;

    private NoOpNotFoundCache nfc;

    private NoOpLocationExpander expander;

    public ApiCDIExtension()
    {
        super( new ExternalContext() );
    }

    public ApiCDIExtension withDefaultComponentInstances()
    {
        events = new NoOpFileEventManager();
        decorator = new NoOpTransferDecorator();
        nfc = new NoOpNotFoundCache();
        expander = new NoOpLocationExpander();

        return this;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public ApiCDIExtension withDefaultBeans()
    {
        with( ExecutorService.class, new ExecutorServiceBean( 2, true, 8, "galley-transfers" ) );
        with( FileEventManager.class, new DefaultInstanceBean( events, FileEventManager.class ) );
        with( TransferDecorator.class, new DefaultInstanceBean( decorator, TransferDecorator.class ) );
        with( NotFoundCache.class, new DefaultInstanceBean( nfc, NotFoundCache.class ) );
        with( LocationExpander.class, new DefaultInstanceBean( expander, LocationExpander.class ) );

        return this;
    }

    public ApiCDIExtension withTestCacheProvider( final TemporaryFolder temp )
    {
        getContext().with( CacheProvider.class,
                           new DefaultInstanceBean<CacheProvider>( new TestCacheProvider( temp.newFolder( "cache" ), events, decorator ),
                                                                   CacheProvider.class ) );
        return this;
    }

    public ApiCDIExtension with( final Class<?> type, final ExternalBean<?> bean )
    {
        getContext().with( type, bean );
        return this;
    }

    public NoOpFileEventManager getEvents()
    {
        return events;
    }

    public NoOpTransferDecorator getDecorator()
    {
        return decorator;
    }

    public NoOpNotFoundCache getNfc()
    {
        return nfc;
    }

    public NoOpLocationExpander getExpander()
    {
        return expander;
    }

    public ApiCDIExtension setEvents( final NoOpFileEventManager events )
    {
        this.events = events;
        return this;
    }

    public ApiCDIExtension setDecorator( final NoOpTransferDecorator decorator )
    {
        this.decorator = decorator;
        return this;
    }

    public ApiCDIExtension setNfc( final NoOpNotFoundCache nfc )
    {
        this.nfc = nfc;
        return this;
    }

    public ApiCDIExtension setExpander( final NoOpLocationExpander expander )
    {
        this.expander = expander;
        return this;
    }

    public <C, T extends C> ApiCDIExtension withDefaultBean( final T instance, final Class<C> type )
    {
        with( type, new DefaultInstanceBean<C>( instance, type ) );
        return this;
    }

}
