package org.commonjava.maven.galley.transport.htcli.testutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

public class TestCacheProvider
    implements CacheProvider
{

    private final File dir;

    private final FileEventManager events;

    private final TransferDecorator decorator;

    public TestCacheProvider( final File dir, final FileEventManager events, final TransferDecorator decorator )
    {
        this.dir = dir;
        this.events = events;
        this.decorator = decorator;
    }

    public Transfer getCacheReference( final Resource resource )
    {
        return new Transfer( resource, this, events, decorator );
    }

    public Transfer writeClasspathResourceToCache( final Resource resource, final String cpResource )
        throws IOException
    {
        final InputStream in = Thread.currentThread()
                                     .getContextClassLoader()
                                     .getResourceAsStream( cpResource );
        if ( in == null )
        {
            throw new IOException( "Classpath resource not found: " + cpResource );
        }

        final Transfer tx = getCacheReference( resource );
        OutputStream out = null;
        try
        {
            out = tx.openOutputStream( TransferOperation.UPLOAD, false );
            IOUtils.copy( in, out );
        }
        finally
        {
            IOUtils.closeQuietly( in );
            IOUtils.closeQuietly( out );
        }

        return tx;
    }

    public Transfer writeToCache( final Resource resource, final String content )
        throws IOException
    {
        if ( content == null )
        {
            throw new IOException( "Content is empty!" );
        }

        final Transfer tx = getCacheReference( resource );
        OutputStream out = null;
        try
        {
            out = tx.openOutputStream( TransferOperation.UPLOAD, false );
            out.write( content.getBytes() );
        }
        finally
        {
            IOUtils.closeQuietly( out );
        }

        return tx;
    }

    @Override
    public boolean isDirectory( final Resource resource )
    {
        return getDetachedFile( resource ).isDirectory();
    }

    @Override
    public InputStream openInputStream( final Resource resource )
        throws IOException
    {
        return new FileInputStream( getDetachedFile( resource ) );
    }

    @Override
    public OutputStream openOutputStream( final Resource resource )
        throws IOException
    {
        final File f = getDetachedFile( resource );
        final File d = f.getParentFile();
        if ( d != null )
        {
            d.mkdirs();
        }

        return new FileOutputStream( f );
    }

    @Override
    public boolean exists( final Resource resource )
    {
        return getDetachedFile( resource ).exists();
    }

    @Override
    public void copy( final Resource from, final Resource to )
        throws IOException
    {
        final File ff = getDetachedFile( from );
        final File tf = getDetachedFile( to );
        if ( ff.isDirectory() )
        {
            FileUtils.copyDirectory( ff, tf );
        }
        else
        {
            FileUtils.copyFile( ff, tf );
        }
    }

    @Override
    public String getFilePath( final Resource resource )
    {
        return getDetachedFile( resource ).getPath();
    }

    @Override
    public boolean delete( final Resource resource )
        throws IOException
    {
        FileUtils.forceDelete( getDetachedFile( resource ) );
        return true;
    }

    @Override
    public String[] list( final Resource resource )
    {
        return getDetachedFile( resource ).list();
    }

    @Override
    public File getDetachedFile( final Resource resource )
    {
        return new File( new File( dir, resource.getLocationName() ), resource.getPath() );
    }

    @Override
    public void mkdirs( final Resource resource )
        throws IOException
    {
        getDetachedFile( resource ).mkdirs();
    }

    @Override
    public void createFile( final Resource resource )
        throws IOException
    {
        getDetachedFile( resource ).createNewFile();
    }

    @Override
    public void createAlias( final Resource from, final Resource to )
        throws IOException
    {
        final File fromFile = getDetachedFile( from );
        final File toFile = getDetachedFile( to );
        Files.createLink( Paths.get( fromFile.toURI() ), Paths.get( toFile.toURI() ) );
    }

}
