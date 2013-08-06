package org.commonjava.maven.galley.model;

import static org.apache.commons.lang.StringUtils.join;

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

public final class Transfer
{

    public static final String ROOT = "/";

    private static final String[] ROOT_ARRY = { ROOT };

    private final Location loc;

    private final String path;

    private final CacheProvider provider;

    private final TransferDecorator decorator;

    private final FileEventManager fileEventManager;

    public Transfer( final Location loc, final CacheProvider provider, final FileEventManager fileEventManager,
                     final TransferDecorator decorator, final String... path )
    {
        this.loc = loc;
        this.fileEventManager = fileEventManager;
        this.decorator = decorator;
        this.path = normalize( join( path, "/" ) );
        this.provider = provider;
    }

    private String normalize( final String path )
    {
        String result = path;
        while ( result.startsWith( "/" ) && result.length() > 1 )
        {
            result = result.substring( 1 );
        }

        return result;
    }

    public boolean isDirectory()
    {
        return provider.isDirectory( loc, path );
    }

    public Location getLocation()
    {
        return loc;
    }

    public String getPath()
    {
        return path;
    }

    @Override
    public String toString()
    {
        return loc + ":" + path;
    }

    public Transfer getParent()
    {
        if ( path == ROOT || ROOT.equals( path ) )
        {
            return null;
        }

        return new Transfer( loc, provider, fileEventManager, decorator, parentPath( path ) );
    }

    public Transfer getChild( final String file )
    {
        return new Transfer( loc, provider, fileEventManager, decorator, path, file );
    }

    private String[] parentPath( final String path )
    {
        final String[] parts = path.split( "/" );
        if ( parts.length == 1 )
        {
            return ROOT_ARRY;
        }
        else
        {
            final String[] parentParts = new String[parts.length - 1];
            System.arraycopy( parts, 0, parentParts, 0, parentParts.length );
            return parentParts;
        }
    }

    public void touch()
    {
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
        try
        {
            InputStream stream = provider.openInputStream( loc, path );
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
        try
        {
            OutputStream stream = provider.openOutputStream( loc, path );
            if ( stream == null )
            {
                return null;
            }

            stream = decorator.decorateWrite( stream, accessType );
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
        return provider.exists( loc, path );
    }

    public void copyFrom( final Transfer f )
        throws IOException
    {
        provider.copy( f.getLocation(), f.getPath(), loc, path );
    }

    public String getFullPath()
    {
        return provider.getFilePath( loc, path );
    }

    public boolean delete()
        throws IOException
    {
        return delete( true );
    }

    public boolean delete( final boolean fireEvents )
        throws IOException
    {
        try
        {
            final boolean deleted = provider.delete( loc, path );
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
        return provider.list( loc, path );
    }

    public File getDetachedFile()
    {
        return provider.getDetachedFile( loc, path );
    }

    public void mkdirs()
        throws IOException
    {
        provider.mkdirs( loc, path );
    }

    public void createFile()
        throws IOException
    {
        provider.createFile( loc, path );
    }

}
