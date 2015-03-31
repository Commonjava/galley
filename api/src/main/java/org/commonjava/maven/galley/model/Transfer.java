/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.event.FileAccessEvent;
import org.commonjava.maven.galley.event.FileDeletionEvent;
import org.commonjava.maven.galley.event.FileErrorEvent;
import org.commonjava.maven.galley.event.FileStorageEvent;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.util.TransferUnlockingOutputStream;

public final class Transfer
{

    private final ConcreteResource resource;

    private final CacheProvider provider;

    private final TransferDecorator decorator;

    private final FileEventManager fileEventManager;

    public Transfer( final Location loc, final CacheProvider provider, final FileEventManager fileEventManager,
                     final TransferDecorator decorator, final String... path )
    {
        this.resource = new ConcreteResource( loc, path );
        this.fileEventManager = fileEventManager;
        this.decorator = decorator;
        this.provider = provider;
    }

    public Transfer( final ConcreteResource resource, final CacheProvider provider,
                     final FileEventManager fileEventManager, final TransferDecorator decorator )
    {
        this.resource = resource;
        this.fileEventManager = fileEventManager;
        this.decorator = decorator;
        this.provider = provider;
    }

    public boolean isDirectory()
    {
        return provider.isDirectory( resource );
    }

    public boolean isFile()
    {
        return provider.isFile( resource );
    }

    public Location getLocation()
    {
        return resource.getLocation();
    }

    public String getPath()
    {
        return resource.getPath();
    }

    public ConcreteResource getResource()
    {
        return resource;
    }

    @Override
    public String toString()
    {
        return String.format( "%s:%s", resource.getLocation(), resource.getPath() );
    }

    public Transfer getParent()
    {
        if ( resource.isRoot() )
        {
            return null;
        }

        return provider.getTransfer( (ConcreteResource) resource.getParent() );
    }

    public Transfer getChild( final String file )
    {
        return provider.getTransfer( (ConcreteResource) resource.getChild( file ) );
    }

    public void touch()
    {
        provider.waitForWriteUnlock( resource );
        if ( decorator != null )
        {
            decorator.decorateTouch( this );
        }
        fileEventManager.fire( new FileAccessEvent( this ) );
    }

    public InputStream openInputStream()
        throws IOException
    {
        return openInputStream( true );
    }

    public InputStream openInputStream( final boolean fireEvents )
        throws IOException
    {
        provider.waitForReadUnlock( resource );
        try
        {
            InputStream stream = provider.openInputStream( resource );
            if ( stream == null )
            {
                return null;
            }

            stream = decorator == null ? stream : decorator.decorateRead( stream, this );
            if ( fireEvents )
            {
                fileEventManager.fire( new FileAccessEvent( this ) );
            }
            return stream;
        }
        catch ( final IOException e )
        {
            if ( fireEvents )
            {
                fileEventManager.fire( new FileErrorEvent( this, e ) );
            }
            throw e;
        }
    }

    public OutputStream openOutputStream( final TransferOperation accessType )
        throws IOException
    {
        return openOutputStream( accessType, true );
    }

    public OutputStream openOutputStream( final TransferOperation accessType, final boolean fireEvents )
        throws IOException
    {
        provider.waitForWriteUnlock( resource );
        try
        {
            provider.lockWrite( resource );
            OutputStream stream = provider.openOutputStream( resource );
            if ( stream == null )
            {
                return null;
            }

            final TransferUnlocker unlocker = new TransferUnlocker( resource, provider );
            stream = new TransferUnlockingOutputStream( stream, unlocker );
            stream = decorator == null ? stream : decorator.decorateWrite( stream, this, accessType );

            if ( fireEvents )
            {
                fileEventManager.fire( new FileStorageEvent( accessType, this ) );
            }

            return stream;
        }
        catch ( final IOException e )
        {
            if ( fireEvents )
            {
                fileEventManager.fire( new FileErrorEvent( this, e ) );
            }
            throw e;
        }
    }

    public boolean exists()
    {
        if ( decorator != null )
        {
            decorator.decorateExists( this );
        }
        return provider.exists( resource );
    }

    public void copyFrom( final Transfer f )
        throws IOException
    {
        provider.waitForWriteUnlock( resource );
        provider.lockWrite( resource );
        try
        {
            if ( decorator != null )
            {
                decorator.decorateCopyFrom( f, this );
            }
            provider.copy( f.getResource(), resource );
        }
        finally
        {
            provider.unlockWrite( resource );
        }
    }

    public String getFullPath()
    {
        return provider.getFilePath( resource );
    }

    public boolean delete()
        throws IOException
    {
        return delete( true );
    }

    public boolean delete( final boolean fireEvents )
        throws IOException
    {
        provider.waitForWriteUnlock( resource );
        try
        {
            if ( decorator != null )
            {
                decorator.decorateDelete( this );
            }

            final boolean deleted = provider.delete( resource );
            if ( deleted && fireEvents )
            {
                fileEventManager.fire( new FileDeletionEvent( this ) );
            }

            return deleted;
        }
        catch ( final IOException e )
        {
            if ( fireEvents )
            {
                fileEventManager.fire( new FileErrorEvent( this, e ) );
            }
            throw e;
        }
    }

    public String[] list()
        throws IOException
    {
        String[] listing = provider.list( resource );
        if ( decorator != null )
        {
            listing = decorator.decorateListing( this, listing );
        }
        return listing;
    }

    public File getDetachedFile()
    {
        provider.waitForWriteUnlock( resource );
        provider.lockWrite( resource );
        try
        {
            return provider.getDetachedFile( resource );
        }
        finally
        {
            provider.unlockWrite( resource );
        }
    }

    public void mkdirs()
        throws IOException
    {
        if ( decorator != null )
        {
            decorator.decorateMkdirs( this );
        }
        provider.mkdirs( resource );
    }

    public void createFile()
        throws IOException
    {
        provider.waitForWriteUnlock( resource );
        if ( decorator != null )
        {
            decorator.decorateCreateFile( this );
        }
        provider.createFile( resource );
    }

    public long length()
    {
        return provider.length( resource );
    }

    public long lastModified()
    {
        return provider.lastModified( resource );
    }

    public Transfer getSibling( final String named )
    {
        return getParent().getChild( named );
    }

    public Transfer getSiblingMeta( final String extension )
    {
        final String named = new File( resource.getPath() ).getName() + extension;
        return getParent().getChild( named );
    }

    public static final class TransferUnlocker
    {
        private final CacheProvider provider;

        private final ConcreteResource resource;

        private TransferUnlocker( final ConcreteResource resource, final CacheProvider provider )
        {
            this.resource = resource;
            this.provider = provider;
        }

        public void unlock()
        {
            provider.unlockWrite( resource );
        }
    }

}
