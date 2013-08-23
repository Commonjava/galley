package org.commonjava.maven.galley.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.io.PathGenerator;

@Named( "file-galley-cache" )
public class FileCacheProvider
    implements CacheProvider
{

    @Inject
    private FileCacheProviderConfig config;

    protected FileCacheProvider()
    {
    }

    public FileCacheProvider( final File cacheBasedir, final PathGenerator pathGenerator, final boolean aliasLinking )
    {
        this.config = new FileCacheProviderConfig( cacheBasedir ).withAliasLinking( aliasLinking )
                                                                 .withPathGenerator( pathGenerator );
    }

    public FileCacheProvider( final FileCacheProviderConfig config )
    {
        this.config = config;
    }

    public FileCacheProvider( final File cacheBasedir, final PathGenerator pathGenerator )
    {
        this( cacheBasedir, pathGenerator, true );
    }

    @Override
    public File getDetachedFile( final Resource resource )
    {
        return new File( getFilePath( resource ) );
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
        final File file = getDetachedFile( resource );

        final File dir = file.getParentFile();
        if ( !dir.isDirectory() && !dir.mkdirs() )
        {
            throw new IOException( "Cannot create directory: " + dir );
        }

        return new FileOutputStream( file );
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
        FileUtils.copyFile( getDetachedFile( from ), getDetachedFile( to ) );
    }

    @Override
    public boolean delete( final Resource resource )
        throws IOException
    {
        return getDetachedFile( resource ).delete();
    }

    @Override
    public String[] list( final Resource resource )
    {
        return getDetachedFile( resource ).list();
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
    public void createAlias( final Resource to, final Resource from )
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
            if ( config.isAliasLinking() )
            {
                final File fromFile = getDetachedFile( from );
                final File toFile = getDetachedFile( to );

                Files.createLink( Paths.get( fromFile.toURI() ), Paths.get( toFile.toURI() ) );
            }
            else
            {
                copy( from, to );
            }
        }
    }

    @Override
    public String getFilePath( final Resource resource )
    {
        return Paths.get( config.getCacheBasedir()
                                .getPath(), config.getPathGenerator()
                                                  .getFilePath( resource ) )
                    .toString();
    }
}
