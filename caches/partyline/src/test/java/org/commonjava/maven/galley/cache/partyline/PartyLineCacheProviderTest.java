package org.commonjava.maven.galley.cache.partyline;

import org.commonjava.maven.galley.cache.CacheProviderTCK;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class PartyLineCacheProviderTest
    extends CacheProviderTCK
{

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private PartyLineCacheProvider provider;

    @Before
    public void setup()
        throws Exception
    {
        final PathGenerator pathgen = new HashedLocationPathGenerator();
        final FileEventManager events = new NoOpFileEventManager();
        final NoOpTransferDecorator decorator = new NoOpTransferDecorator();

        provider = new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator );
    }

    @Override
    protected CacheProvider getCacheProvider()
        throws Exception
    {
        return provider;
    }

}
