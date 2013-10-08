package org.commonjava.maven.galley.cache;

import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class FileCacheProviderTest
    extends CacheProviderTCK
{

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Override
    protected CacheProvider getCacheProvider()
        throws Exception
    {
        return new FileCacheProvider( temp.newFolder( "cache" ), new HashedLocationPathGenerator(), new NoOpFileEventManager(),
                                      new NoOpTransferDecorator(), true );
    }

}
