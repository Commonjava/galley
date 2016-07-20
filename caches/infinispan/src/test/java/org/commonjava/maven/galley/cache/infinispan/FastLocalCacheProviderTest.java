package org.commonjava.maven.galley.cache.infinispan;

import org.commonjava.cdi.util.weft.WeftManaged;
import org.commonjava.maven.galley.cache.CacheProviderTCK;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.cache.testutil.TestFileEventManager;
import org.commonjava.maven.galley.cache.testutil.TestTransferDecorator;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 */
public class FastLocalCacheProviderTest
        extends CacheProviderTCK
{
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    private FastLocalCacheProvider provider;

    private static EmbeddedCacheManager CACHE_MANAGER;

    @BeforeClass
    public static void setupClass()
    {
        CACHE_MANAGER = new NFSOwnerCacheProducer().getCacheMgr();
    }

    @Before
    public void setup()
            throws Exception
    {
        final PathGenerator pathgen = new HashedLocationPathGenerator();
        final FileEventManager events = new TestFileEventManager();
        final TransferDecorator decorator = new TestTransferDecorator();

        Cache<String, String> cache = CACHE_MANAGER.getCache( NFSOwnerCacheProducer.CACHE_NAME );

        final String nfsBasePath = createNFSBaseDir( temp.newFolder().getAbsolutePath() );

        final Executor executor = Executors.newFixedThreadPool( 5 );
        provider =
                new FastLocalCacheProvider( new PartyLineCacheProvider( temp.newFolder(), pathgen, events, decorator ),
                                            cache, events, decorator, executor, nfsBasePath );
        provider.init();
    }

    @Override
    protected CacheProvider getCacheProvider()
            throws Exception
    {
        return provider;
    }

    private String createNFSBaseDir( String tempBaseDir )
    {
        File file = new File( tempBaseDir + "/mnt/nfs" );
        file.delete();
        file.mkdir();
        return file.getAbsolutePath();
    }

    @After
    public void tearDown()
    {
        provider.destroy();
    }

}
