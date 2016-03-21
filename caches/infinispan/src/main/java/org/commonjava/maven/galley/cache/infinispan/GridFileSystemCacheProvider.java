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
package org.commonjava.maven.galley.cache.infinispan;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.infinispan.io.GridFilesystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Named( "partyline-galley-cache" )
@Alternative
public class GridFileSystemCacheProvider
        implements CacheProvider, CacheProvider.AdminView
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private GridFilesystem filesystem;

    @Inject
    private PathGenerator pathGenerator;

    @Inject
    private FileEventManager fileEventManager;

    @Inject
    private TransferDecorator transferDecorator;

    protected GridFileSystemCacheProvider()
    {
    }

    public GridFileSystemCacheProvider( final PathGenerator pathGenerator, final FileEventManager fileEventManager,
                                        final TransferDecorator transferDecorator, GridFilesystem filesystem )
    {
        this.pathGenerator = pathGenerator;
        this.fileEventManager = fileEventManager;
        this.transferDecorator = transferDecorator;
        this.filesystem = filesystem;
    }

    public boolean isFileBased()
    {
        return true;
    }

    @Override
    public File getDetachedFile( final ConcreteResource resource )
    {
        File f = filesystem.getFile( getFilePath( resource ) );
        if ( resource.isRoot() && !f.isDirectory() )
        {
            f.mkdirs();
        }

        return f;
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
        String path = getFilePath( resource );
        logger.debug( "Opening input stream to: {}", path );
        return filesystem.getInput( path );
    }

    @Override
    public OutputStream openOutputStream( final ConcreteResource resource )
            throws IOException
    {
        File dir = getDetachedFile( resource ).getParentFile();
        logger.debug( "Verifying that parent directory exists: " + dir );
        if ( !dir.isDirectory() && !dir.mkdirs() )
        {
            throw new IOException( "Cannot create directory: " + dir );
        }

        String path = getFilePath( resource );
        logger.debug( "Opening output stream to: {}", path );

        return filesystem.getOutput( path );
    }

    @Override
    public boolean exists( final ConcreteResource resource )
    {
        final File f = getDetachedFile( resource );
        return f.exists();
    }

    @Override
    public void copy( final ConcreteResource from, final ConcreteResource to )
            throws IOException
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = openInputStream( from );
            out = openOutputStream( to );
            int copied = IOUtils.copy( in, out );
            logger.debug( "{} bytes copied.", copied );
        }
        finally
        {
            IOUtils.closeQuietly( out );
            IOUtils.closeQuietly( in );
        }
    }

    @Override
    public boolean delete( final ConcreteResource resource )
            throws IOException
    {
        return getDetachedFile( resource ).delete();
    }

    @Override
    public String[] list( final ConcreteResource resource )
    {
        final String[] listing = getDetachedFile( resource ).list();
        if ( listing == null )
        {
            return null;
        }

        final List<String> list = new ArrayList<String>( Arrays.asList( listing ) );
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

        return list.toArray( new String[list.size()] );
    }

    @Override
    public void mkdirs( final ConcreteResource resource )
            throws IOException
    {
        getDetachedFile( resource ).mkdirs();
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
        return pathGenerator.getFilePath( resource );
    }

    @Override
    public synchronized Transfer getTransfer( final ConcreteResource resource )
    {
        return new Transfer( resource, this, fileEventManager, transferDecorator );
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
        // TODO
        return false;
    }

    @Override
    public boolean isWriteLocked( final ConcreteResource resource )
    {
        // TODO
        return false;
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
        // TODO
    }

    @Override
    public void waitForReadUnlock( final ConcreteResource resource )
    {
        // TODO
    }

    @Override
    public void cleanupCurrentThread()
    {
    }

    @Override
    public void startReporting()
    {
    }

    @Override
    public void stopReporting()
    {
    }
}
