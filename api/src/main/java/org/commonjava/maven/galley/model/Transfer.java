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
package org.commonjava.maven.galley.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.event.FileAccessEvent;
import org.commonjava.maven.galley.event.FileDeletionEvent;
import org.commonjava.maven.galley.event.FileErrorEvent;
import org.commonjava.maven.galley.event.FileStorageEvent;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.util.TransferInputStream;
import org.commonjava.maven.galley.util.TransferOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Transfer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

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
            return this;
        }

        return provider.getTransfer( (ConcreteResource) resource.getParent() );
    }

    public Transfer getChild( final String file )
    {
        return provider.getTransfer( (ConcreteResource) resource.getChild( file ) );
    }

    public void touch()
    {
        touch( new EventMetadata() );
    }

    public void touch( final EventMetadata eventMetadata )
    {
        provider.waitForWriteUnlock( resource );
        if ( decorator != null )
        {
            decorator.decorateTouch( this );
        }
        fileEventManager.fire( new FileAccessEvent( this, eventMetadata ) );
    }

    public InputStream openInputStream()
        throws IOException
    {
        return openInputStream( true, new EventMetadata() );
    }

    public InputStream openInputStream( final boolean fireEvents )
        throws IOException
    {
        return openInputStream( fireEvents, new EventMetadata() );
    }

    public InputStream openInputStream( final boolean fireEvents, final EventMetadata eventMetadata )
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

            if ( fireEvents )
            {
                stream = new TransferInputStream( stream, new FileAccessEvent( this, eventMetadata ), fileEventManager );
            }

            stream = decorator == null ? stream : decorator.decorateRead( stream, this );
            return stream;
        }
        catch ( final IOException e )
        {
            if ( fireEvents )
            {
                fileEventManager.fire( new FileErrorEvent( this, e, eventMetadata ) );
            }
            throw e;
        }
    }

    public OutputStream openOutputStream( final TransferOperation accessType )
        throws IOException
    {
        return openOutputStream( accessType, true, new EventMetadata() );
    }

    public OutputStream openOutputStream( final TransferOperation accessType, final boolean fireEvents )
        throws IOException
    {
        return openOutputStream( accessType, fireEvents, new EventMetadata() );
    }

    public OutputStream openOutputStream( final TransferOperation accessType, final boolean fireEvents,
                                          final EventMetadata eventMetadata )
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
            if ( fireEvents )
            {
                stream =
                    new TransferOutputStream( stream, unlocker,
                                              new FileStorageEvent( accessType, this, eventMetadata ),
                                              fileEventManager );
            }
            else
            {
                stream = new TransferOutputStream( stream, unlocker );
            }

            stream = decorator == null ? stream : decorator.decorateWrite( stream, this, accessType );

            return stream;
        }
        catch ( final IOException e )
        {
            if ( fireEvents )
            {
                fileEventManager.fire( new FileErrorEvent( this, e, eventMetadata ) );
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
        return delete( true, new EventMetadata() );
    }

    public boolean delete( final boolean fireEvents )
        throws IOException
    {
        return delete( fireEvents, new EventMetadata() );
    }

    public boolean delete( final boolean fireEvents, final EventMetadata eventMetadata )
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
                fileEventManager.fire( new FileDeletionEvent( this, eventMetadata ) );
            }

            return deleted;
        }
        catch ( final IOException e )
        {
            if ( fireEvents )
            {
                fileEventManager.fire( new FileErrorEvent( this, e, eventMetadata ) );
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
        logger.debug( "Creating meta-transfer sibling for: {} with name: {} (parent: {})", this, named, getParent() );
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
