package org.commonjava.maven.galley.live.testutil;

import java.util.concurrent.ExecutorService;

import org.commonjava.maven.galley.cache.FileCacheProviderConfig;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.util.cdi.ConfigurationExtension;
import org.commonjava.util.cdi.DefaultInstanceBean;
import org.commonjava.util.cdi.ExecutorServiceBean;
import org.commonjava.util.cdi.ExternalContext;
import org.junit.rules.TemporaryFolder;

public class TransferManagerExtension
    extends ConfigurationExtension
{

    public TransferManagerExtension( final TemporaryFolder temp )
    {
        super( new ExternalContext() );
        getContext().with( ExecutorService.class, new ExecutorServiceBean( 2, true, 8, "galley-transfers" ) );

        final FileCacheProviderConfig config = new FileCacheProviderConfig( temp.newFolder( "cache" ) );
        getContext().with( FileCacheProviderConfig.class,
                           new DefaultInstanceBean<FileCacheProviderConfig>( config, FileCacheProviderConfig.class ) );

        getContext().with( FileEventManager.class,
                           new DefaultInstanceBean<FileEventManager>( new NoOpFileEventManager(),
                                                                      FileEventManager.class ) );

        getContext().with( TransferDecorator.class,
                           new DefaultInstanceBean<TransferDecorator>( new NoOpTransferDecorator(),
                                                                       TransferDecorator.class ) );
    }

}
