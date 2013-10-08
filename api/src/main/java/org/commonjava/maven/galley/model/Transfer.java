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

    private boolean locked = false;

    public Transfer( final Location loc, final CacheProvider provider, final FileEventManager fileEventManager, final TransferDecorator decorator,
                     final String... path )
    {
        this.resource = new ConcreteResource( loc, path );
        this.fileEventManager = fileEventManager;
        this.decorator = decorator;
        this.provider = provider;
    }

    public Transfer( final ConcreteResource resource, final CacheProvider provider, final FileEventManager fileEventManager,
                     final TransferDecorator decorator )
    {
        this.resource = resource;
        this.fileEventManager = fileEventManager;
        this.decorator = decorator;
        this.provider = provider;
    }

    public boolean isLocked()
    {
        return locked;
    }

    public synchronized void unlock()
    {
        locked = false;
        notifyAll();
    }

    public synchronized void waitForUnlock()
    {
        if ( locked )
        {
            try
            {
                wait();
            }
            catch ( final InterruptedException e )
            {
                // TODO
            }
        }
    }

    public synchronized void waitForUnlock( final long millis )
    {
        if ( locked )
        {
            try
            {
                wait( millis );
            }
            catch ( final InterruptedException e )
            {
                // TODO
            }
        }
    }

    public boolean isDirectory()
    {
        return provider.isDirectory( resource );
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
        waitForUnlock();
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
        waitForUnlock();
        try
        {
            InputStream stream = provider.openInputStream( resource );
            if ( stream == null )
            {
                return null;
            }

            stream = decorator.decorateRead( stream );
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
        waitForUnlock();
        try
        {
            locked = true;
            OutputStream stream = provider.openOutputStream( resource );
            if ( stream == null )
            {
                return null;
            }

            stream = decorator.decorateWrite( new TransferUnlockingOutputStream( stream, this ), accessType );
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
        waitForUnlock();
        return provider.exists( resource );
    }

    public void copyFrom( final Transfer f )
        throws IOException
    {
        waitForUnlock();
        locked = true;
        try
        {
            provider.copy( f.getResource(), resource );
        }
        finally
        {
            unlock();
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
        waitForUnlock();
        try
        {
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
    {
        return provider.list( resource );
    }

    public File getDetachedFile()
    {
        waitForUnlock();
        locked = true;
        try
        {
            return provider.getDetachedFile( resource );
        }
        finally
        {
            unlock();
        }
    }

    public void mkdirs()
        throws IOException
    {
        provider.mkdirs( resource );
    }

    public void createFile()
        throws IOException
    {
        waitForUnlock();
        provider.createFile( resource );
    }

}
