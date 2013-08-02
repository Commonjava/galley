package org.commonjava.maven.galley.testutil;

import java.io.File;
import java.nio.file.Paths;

import org.commonjava.maven.galley.cache.AbstractFileCacheProvider;
import org.commonjava.maven.galley.cache.CacheProvider;
import org.commonjava.maven.galley.model.Location;
import org.junit.rules.TemporaryFolder;

/**
 * Stub {@link CacheProvider} based on {@link AbstractFileCacheProvider} which
 * uses a JUnit {@link TemporaryFolder} instance (passed in from the test class)
 * to construct a cache directory based on the hashCode() of the {@link Location}
 * and the path as a subdir structure.
 * 
 * @author jdcasey
 */
public class TestCacheProvider
    extends AbstractFileCacheProvider
{

    private final File basedir;

    public TestCacheProvider( final TemporaryFolder temp )
    {
        super( true );
        this.basedir = temp.newFolder( "cache" );
    }

    @Override
    public String getFilePath( final Location loc, final String path )
    {
        return Paths.get( basedir.getPath(), Integer.toString( loc.getUri()
                                                                  .hashCode() ), path )
                    .toString();
    }

}
