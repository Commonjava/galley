package org.commonjava.maven.galley.embed;

import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.cache.FileCacheProviderConfig;
import org.commonjava.test.http.TestHttpServer;
import org.commonjava.test.http.expect.ExpectationServer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import java.io.IOException;

/**
 * Created by jdcasey on 9/14/15.
 */
@RunWith(WeldJUnit4Runner.class)
public abstract class AbstractEmbeddableCDIProducerTest
{
}
