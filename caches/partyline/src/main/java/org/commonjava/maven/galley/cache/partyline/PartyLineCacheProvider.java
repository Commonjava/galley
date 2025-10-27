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
package org.commonjava.maven.galley.cache.partyline;

import org.apache.commons.io.IOUtils;
import org.commonjava.cdi.util.weft.SingleThreadedExecutorService;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.util.PathUtils;
import org.commonjava.util.partyline.Partyline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PartyLineCacheProvider
    implements CacheProvider, CacheProvider.AdminView
{
    private static final long SWEEP_TIMEOUT_SECONDS = 30;

    private static final long DELETE_TIMEOUT_MILLIS = 2000;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Partyline fileManager;

    private final PartyLineCacheProviderConfig config;

    private final PathGenerator pathGenerator;

    private final FileEventManager fileEventManager;

    private final TransferDecoratorManager transferDecorator;

    private final List<Transfer> toDelete = Collections.synchronizedList( new ArrayList<>() );

    public PartyLineCacheProvider( final File cacheBasedir, final PathGenerator pathGenerator,
                                   final FileEventManager fileEventManager, final TransferDecoratorManager transferDecorator,
                                   final ScheduledExecutorService deleteExecutor,
                                   final Partyline fileManager)
    {
        this.pathGenerator = pathGenerator;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.config = new PartyLineCacheProviderConfig( cacheBasedir );
        ScheduledExecutorService deleteExecutor1 =
                deleteExecutor == null ? Executors.newScheduledThreadPool( 2 ) : deleteExecutor;
        this.fileManager = fileManager;

        int threads = 2;
        if ( deleteExecutor instanceof ThreadPoolExecutor )
        {
            threads = ( (ThreadPoolExecutor) deleteExecutor ).getPoolSize();
        }
        else if ( deleteExecutor instanceof SingleThreadedExecutorService )
        {
            threads = 1;
        }

        for(int i=0; i<threads; i++)
        {
            Objects.requireNonNull( deleteExecutor )
                   .schedule( newTransferDeleteSweeper(), SWEEP_TIMEOUT_SECONDS, TimeUnit.SECONDS );
        }

        startReportingDaemon();
    }

    @PostConstruct
    public void startReportingDaemon()
    {
        fileManager.startReporting();
    }

    @PreDestroy
    public void stopReportingDaemon()
    {
        fileManager.stopReporting();
    }

    public boolean isFileBased()
    {
        return true;
    }

    private final Object mkdirs_mutex = new Object();

    private boolean makeDirs( File f )
    {
        synchronized ( mkdirs_mutex )
        {
            if ( !f.isDirectory() )
            {
                return f.mkdirs();
            }
            return true;
        }
    }

    @Override
    public File getDetachedFile( final ConcreteResource resource )
    {
        return new File( getFilePath( resource ) );
    }

    @Override
    public boolean isDirectory( final ConcreteResource resource )
    {
        final File f = getDetachedFile( resource );
        return f.isDirectory();
    }

    @Override
    public boolean isFile( final ConcreteResource resource )
    {
        final File f = getDetachedFile( resource );
        return f.isFile();
    }

    @Override
    public InputStream openInputStream( final ConcreteResource resource )
        throws IOException
    {
        final File targetFile = getDetachedFile( resource );
        if ( targetFile.exists() )
        {
            try
            {
                return fileManager.openInputStream( targetFile );
            }
            catch ( InterruptedException e )
            {
                logger.warn( "Interrupted: " + e.getMessage() );
            }
        }

        return null;
    }

    @Override
    public OutputStream openOutputStream( final ConcreteResource resource )
        throws IOException
    {
        final File targetFile = getDetachedFile( resource );

        final File dir = targetFile.getParentFile();

        if ( !dir.isDirectory() && !makeDirs( dir ) )
        {
            throw new IOException( "Cannot create directory: " + dir );
        }

        try
        {
            return fileManager.openOutputStream( targetFile );
        }
        catch ( InterruptedException e )
        {
            logger.warn( "Interrupted: " + e.getMessage() );
        }

        return null;
    }

    @Override
    public boolean exists( final ConcreteResource resource )
    {
        final File f = getDetachedFile( resource );
        logger.debug( "Checking for existence of cache file: {}", f );
        return f.exists();
    }

    @Override
    public void copy( final ConcreteResource from, final ConcreteResource to )
        throws IOException
    {
        copy( getDetachedFile( from ), getDetachedFile( to ) );
    }

    private void copy( final File from, final File to )
        throws IOException
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = fileManager.openInputStream( from );
            out = fileManager.openOutputStream( to );
            IOUtils.copy( in, out );
        }
        catch ( InterruptedException e )
        {
            logger.warn( "Interrupted: {}", e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( in );
            IOUtils.closeQuietly( out );
        }
    }

    @Override
    public boolean delete( final ConcreteResource resource )
        throws IOException
    {
        try
        {
            return fileManager.tryDelete( getDetachedFile( resource ) );
        }
        catch ( InterruptedException e )
        {
            logger.warn( "Interrupted: " + e.getMessage() );
        }

        return false;
        //        return getDetachedFile( resource ).tryDelete();
    }

    @Override
    public String[] list( final ConcreteResource resource )
    {
        final String[] listing = getDetachedFile( resource ).list();
        if ( listing == null )
        {
            return null;
        }

        final List<String> list = new ArrayList<>( Arrays.asList( listing ) );
        for ( final Iterator<String> it = list.iterator(); it.hasNext(); )
        {
            final String fname = it.next();
            if ( fname.charAt( 0 ) == '.' )
            {
                it.remove();
                continue;
            }

            for ( final String suffix : HIDDEN_SUFFIXES )
            {
                if ( fname.endsWith( suffix ) )
                {
                    it.remove();
                }
            }
        }

        return list.toArray( new String[0] );
    }

    @Override
    public void mkdirs( final ConcreteResource resource )
    {
        File f = getDetachedFile( resource );
        makeDirs( f );
    }

    @Override
    public void createFile( final ConcreteResource resource )
        throws IOException
    {
        getDetachedFile( resource ).createNewFile();
    }

    @Override
    public void createAlias( final ConcreteResource from, final ConcreteResource to )
        throws IOException
    {
        // if the download landed in a different repository, copy it to the current one for
        // completeness...
        final Location fromKey = from.getLocation();
        final Location toKey = to.getLocation();
        final String fromPath = from.getPath();
        final String toPath = to.getPath();

        if ( fromKey != null && toKey != null && !fromKey.equals( toKey ) && fromPath != null && toPath != null
            && !fromPath.equals( toPath ) )
        {
            copy( from, to );
        }
    }

    @Override
    public String getFilePath( final ConcreteResource resource )
    {
        String dir = resource.getLocation()
                                      .getAttribute( Location.ATTR_ALT_STORAGE_LOCATION, String.class );

        if ( dir == null )
        {
            dir = config.getCacheBasedir().getPath();
        }

        return PathUtils.normalize( dir, pathGenerator.getFilePath( resource ) );
    }

    @Override
    public String getStoragePath( final ConcreteResource resource){
        return pathGenerator.getPath( resource );
    }

    @Override
    public Transfer getTransfer( final ConcreteResource resource )
    {
        Transfer txfr = new Transfer( resource, this, fileEventManager, transferDecorator );
        File f = new File( getFilePath( resource ) );

        final int timeoutSeconds =
                resource.getLocation()
                        .getAttribute( Location.CACHE_TIMEOUT_SECONDS, Integer.class, config.getDefaultTimeoutSeconds() );

        if ( !resource.isRoot() && f.exists() && !f.isDirectory() && config.isTimeoutProcessingEnabled()
                && timeoutSeconds > 0 )
        {
            if ( isTimedOut( txfr, timeoutSeconds ) )
            {
                toDelete.add( txfr );
            }
        }

        return txfr;
    }

    private Runnable newTransferDeleteSweeper()
    {
        return ()->
        {
            if ( toDelete.isEmpty() )
            {
                return;
            }

            Transfer transfer = toDelete.remove( 0 );
            final int timeoutSeconds =
                    transfer.getResource().getLocation()
                            .getAttribute( Location.CACHE_TIMEOUT_SECONDS, Integer.class, config.getDefaultTimeoutSeconds() );

            if ( !transfer.getResource().isRoot() && transfer.exists() && !transfer.isDirectory() && config.isTimeoutProcessingEnabled()
                    && timeoutSeconds > 0 )
            {
                if ( isTimedOut( transfer, timeoutSeconds ) )
                {
                    final File f = new File( getFilePath( transfer.getResource() ) );
                    try
                    {
                        logger.info( "Deleting cached file: {}", f );

                        if ( f.exists() )
                        {
                            boolean deleted = fileManager.tryDelete( f, DELETE_TIMEOUT_MILLIS );
                            //                                    FileUtils.forceDelete( mved );
                            if ( !deleted )
                            {
                                logger.warn( "Deletion failed for: {}. Retrying.", f );
                                toDelete.add( transfer );
                            }
                        }
                    }
                    catch ( final IOException | InterruptedException e )
                    {
                        logger.error( String.format( "Failed to tryDelete: %s.", f ), e );
                    }
                }
            }
        };
    }

    private boolean isTimedOut( final Transfer txfr, int timeoutSeconds )
    {
        final long current = System.currentTimeMillis();
        final long lastModified = txfr.lastModified();
        final int tos = Math.max( timeoutSeconds, Location.MIN_CACHE_TIMEOUT_SECONDS );

        final long timeout = TimeUnit.MILLISECONDS.convert( tos, TimeUnit.SECONDS );

        return current - lastModified > timeout;
    }

    @Override
    public void clearTransferCache()
    {
    }

    @Override
    public long length( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).length();
    }

    @Override
    public long lastModified( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).lastModified();
    }

    @Override
    public boolean isReadLocked( final ConcreteResource resource )
    {
        return fileManager.isReadLocked( getDetachedFile( resource ) );
    }

    @Override
    public boolean isWriteLocked( final ConcreteResource resource )
    {
        return fileManager.isWriteLocked( getDetachedFile( resource ) );
    }

    @Override
    public void unlockRead( final ConcreteResource resource )
    {
        //        fileManager.unlock( getDetachedFile( resource ) );
    }

    @Override
    public void unlockWrite( final ConcreteResource resource )
    {
        //        fileManager.unlock( getDetachedFile( resource ) );
    }

    @Override
    public void lockRead( final ConcreteResource resource )
    {
        //        fileManager.lock( getDetachedFile( resource ) );
    }

    @Override
    public void lockWrite( final ConcreteResource resource )
    {
        //        fileManager.lock( getDetachedFile( resource ) );
    }

    @Override
    public void waitForWriteUnlock( final ConcreteResource resource )
    {
        try
        {
            fileManager.waitForWriteUnlock( getDetachedFile( resource ) );
        }
        catch ( InterruptedException e )
        {
            logger.warn( "Interrupted: " + e.getMessage() );
        }
    }

    @Override
    public void waitForReadUnlock( final ConcreteResource resource )
    {
        try
        {
            fileManager.waitForReadUnlock( getDetachedFile( resource ) );
        }
        catch ( InterruptedException e )
        {
            logger.warn( "Interrupted: " + e.getMessage() );
        }
    }

    @Override
    public AdminView asAdminView()
    {
        return this;
    }

    @Override
    public void cleanupCurrentThread()
    {
        fileManager.cleanupCurrentThread();
    }

    @Override
    public void startReporting()
    {
        fileManager.startReporting();
    }

    @Override
    public void stopReporting()
    {
        fileManager.stopReporting();
    }

    public PartyLineCacheProviderConfig getConfig()
    {
        return config;
    }
}
