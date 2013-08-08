package org.commonjava.maven.galley.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.io.PathGenerator;

@ApplicationScoped
@Named( "file" )
public class FileCacheProvider
    implements CacheProvider
{

    @Inject
    @Named( "galley-cache-aliasLinking" )
    private boolean aliasLinking;

    @Inject
    @Named( "galley-cache-basedir" )
    private File cacheBasedir;

    @Inject
    private PathGenerator pathGenerator;

    protected FileCacheProvider()
    {
    }

    public FileCacheProvider( final File cacheBasedir, final PathGenerator pathGenerator, final boolean aliasLinking )
    {
        this.cacheBasedir = cacheBasedir;
        this.pathGenerator = pathGenerator;
        this.aliasLinking = aliasLinking;
    }

    public FileCacheProvider( final File cacheBasedir, final PathGenerator pathGenerator )
    {
        this( cacheBasedir, pathGenerator, true );
    }

    protected final boolean useAliasLinking()
    {
        return aliasLinking;
    }

    @Override
    public File getDetachedFile( final Location loc, final String path )
    {
        return new File( getFilePath( loc, path ) );
    }

    @Override
    public boolean isDirectory( final Location loc, final String path )
    {
        return getDetachedFile( loc, path ).isDirectory();
    }

    @Override
    public InputStream openInputStream( final Location loc, final String path )
        throws IOException
    {
        return new FileInputStream( getDetachedFile( loc, path ) );
    }

    @Override
    public OutputStream openOutputStream( final Location loc, final String path )
        throws IOException
    {
        final File file = getDetachedFile( loc, path );

        final File dir = file.getParentFile();
        if ( !dir.isDirectory() && !dir.mkdirs() )
        {
            throw new IOException( "Cannot create directory: " + dir );
        }

        return new FileOutputStream( file );
    }

    @Override
    public boolean exists( final Location loc, final String path )
    {
        return getDetachedFile( loc, path ).exists();
    }

    @Override
    public void copy( final Location fromKey, final String fromPath, final Location toKey, final String toPath )
        throws IOException
    {
        FileUtils.copyFile( getDetachedFile( fromKey, fromPath ), getDetachedFile( toKey, toPath ) );
    }

    @Override
    public boolean delete( final Location loc, final String path )
        throws IOException
    {
        return getDetachedFile( loc, path ).delete();
    }

    @Override
    public String[] list( final Location loc, final String path )
    {
        return getDetachedFile( loc, path ).list();
    }

    @Override
    public void mkdirs( final Location loc, final String path )
        throws IOException
    {
        getDetachedFile( loc, path ).mkdirs();
    }

    @Override
    public void createFile( final Location loc, final String path )
        throws IOException
    {
        getDetachedFile( loc, path ).createNewFile();
    }

    @Override
    public void createAlias( final Location toKey, final String toPath, final Location fromKey, final String fromPath )
        throws IOException
    {
        // if the download landed in a different repository, copy it to the current one for
        // completeness...
        if ( fromKey != null && toKey != null && !fromKey.equals( toKey ) && fromPath != null && toPath != null
            && !fromPath.equals( toPath ) )
        {
            if ( aliasLinking )
            {
                final File from = getDetachedFile( fromKey, fromPath );
                final File to = getDetachedFile( toKey, toPath );

                Files.createLink( Paths.get( from.toURI() ), Paths.get( to.toURI() ) );
            }
            else
            {
                copy( toKey, toPath, fromKey, fromPath );
            }
        }
    }

    @Override
    public String getFilePath( final Location loc, final String path )
    {
        return Paths.get( cacheBasedir.getPath(), pathGenerator.getFilePath( loc, path ) )
                    .toString();
    }
}
