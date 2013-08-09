package org.commonjava.maven.galley.live.testutil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.commonjava.maven.galley.cache.FileCacheProviderConfig;
import org.commonjava.util.logging.Logger;

@ApplicationScoped
public class TestProvider
{
    //    abd.addBean( new ExecutorServiceBean( 2, true, 8, "galley-transfers", bm ) );
    //    abd.addBean( new DefaultInstanceBean<FileCacheProviderConfig>(
    //                                                                   new FileCacheProviderConfig(
    //                                                                                                temp.newFolder( "cache" ) ),
    //                                                                   FileCacheProviderConfig.class, bm ) );

    private ExecutorService exec;

    private FileCacheProviderConfig config;

    private File cacheDir;

    @PostConstruct
    public void setup()
        throws Exception
    {
        exec = Executors.newFixedThreadPool( 2, new ThreadFactory()
        {
            int idx = 0;

            @Override
            public Thread newThread( final Runnable r )
            {
                final Thread t = new Thread( r, "galley-transfers-" + ( idx++ ) );
                t.setPriority( 8 );
                t.setDaemon( true );

                return t;
            }
        } );

        cacheDir = File.createTempFile( "cache.", ".dir" );
        cacheDir.delete();
        cacheDir.mkdirs();

        config = new FileCacheProviderConfig( cacheDir );
    }

    @PreDestroy
    public void shutdown()
    {
        if ( cacheDir != null )
        {
            try
            {
                FileUtils.forceDelete( cacheDir );
            }
            catch ( final IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Produces
    @TestData
    @Named( "galley-transfers" )
    public ExecutorService getTransferExecutor()
    {
        return exec;
    }

    @Produces
    @TestData
    @Default
    public FileCacheProviderConfig getCacheConfig()
    {
        new Logger( getClass() ).info( "Providing config: %s", config );
        return config;
    }
}
