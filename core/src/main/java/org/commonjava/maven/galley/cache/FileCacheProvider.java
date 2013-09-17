package org.commonjava.maven.galley.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.util.logging.Logger;

@Named( "file-galley-cache" )
public class FileCacheProvider
    implements CacheProvider
{

    private final Logger logger = new Logger( getClass() );

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
    public File getDetachedFile( final ConcreteResource resource )
    {
        final File f = new File( getFilePath( resource ) );

        if ( !resource.isRoot() && f.exists() && !f.isDirectory() )
        {
            final long current = System.currentTimeMillis();
            final long lastModified = f.lastModified();
            final int tos =
                resource.getTimeoutSeconds() < Location.MIN_CACHE_TIMEOUT_SECONDS ? Location.MIN_CACHE_TIMEOUT_SECONDS : resource.getTimeoutSeconds();

            final long timeout = TimeUnit.MILLISECONDS.convert( tos, TimeUnit.SECONDS );

            if ( current - lastModified > timeout )
            {
                final File mved = new File( f.getPath() + ".to-delete" );
                f.renameTo( mved );

                try
                {
                    logger.info( "Deleting cached file: %s (moved to: %s)\n  due to timeout after: %s\n  elapsed: %s\n  original timeout in seconds: %s",
                                 f, mved, timeout, ( current - lastModified ), tos );

                    FileUtils.forceDelete( mved );
                }
                catch ( final IOException e )
                {
                    logger.error( "Failed to delete: %s.", e, f );
                }
            }
        }

        return f;
    }

    @Override
    public boolean isDirectory( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).isDirectory();
    }

    @Override
    public InputStream openInputStream( final ConcreteResource resource )
        throws IOException
    {
        return new FileInputStream( getDetachedFile( resource ) );
    }

    @Override
    public OutputStream openOutputStream( final ConcreteResource resource )
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
    public boolean exists( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).exists();
    }

    @Override
    public void copy( final ConcreteResource from, final ConcreteResource to )
        throws IOException
    {
        FileUtils.copyFile( getDetachedFile( from ), getDetachedFile( to ) );
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
        return getDetachedFile( resource ).list();
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

        if ( fromKey != null && toKey != null && !fromKey.equals( toKey ) && fromPath != null && toPath != null && !fromPath.equals( toPath ) )
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
    public String getFilePath( final ConcreteResource resource )
    {
        return Paths.get( config.getCacheBasedir()
                                .getPath(), config.getPathGenerator()
                                                  .getFilePath( resource ) )
                    .toString();
    }

}
