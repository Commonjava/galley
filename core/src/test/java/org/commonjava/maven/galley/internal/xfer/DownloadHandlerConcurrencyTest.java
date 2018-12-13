/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.internal.xfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.auth.MemoryPasswordManager;
import org.commonjava.maven.galley.cache.FileCacheProvider;
import org.commonjava.maven.galley.config.TransportManagerConfig;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.transport.htcli.HttpClientTransport;
import org.commonjava.maven.galley.transport.htcli.HttpImpl;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.commonjava.test.http.expect.ExpectationHandler;
import org.commonjava.test.http.expect.ExpectationServer;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMRules;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jdcasey on 9/26/17.
 */
@RunWith( BMUnitRunner.class )
@BMUnitConfig( debug = true )
public class DownloadHandlerConcurrencyTest
{
    @Rule
    public ExpectationServer server = new ExpectationServer();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private HttpClientTransport transport;

    private DownloadHandler handler;

    private FileCacheProvider cacheProvider;

    private ExecutorService executor;

    @Before
    public void before()
            throws IOException
    {
        executor = Executors.newCachedThreadPool();

        cacheProvider =
                new FileCacheProvider( temp.newFolder(), new HashedLocationPathGenerator(), new NoOpFileEventManager(),
                                       new NoOpTransferDecorator(), false );

        transport = new HttpClientTransport( new HttpImpl( new MemoryPasswordManager() ), new ObjectMapper(),
                                                             new GlobalHttpConfiguration(), null, null );
    }

    @BMRules( rules = { @BMRule( name = "init rendezvous", targetClass = "DownloadHandler", targetMethod = "<init>",
                                 targetLocation = "ENTRY",
                                 action = "debug(\"Creating rendezvous\"); createRendezvous(\"tsync\", 4);" ),
            @BMRule( name = "rendezvous threads", targetClass = "DownloadHandler", targetMethod = "joinOrStart",
                     targetLocation = "ENTRY",
                     action = "debug(\"waiting for rendezvous\"); rendezvous(\"tsync\"); debug(\"proceeding\");" ) } )
    @Test
    public void concurrentDownloadWaitsThenUsesFirstResult_SmallFile()
            throws Exception
    {
        final NotFoundCache nfc = new MemoryNotFoundCache();
        final TransportManagerConfig mgrConfig = new TransportManagerConfig();
        handler = new DownloadHandler( nfc, mgrConfig, executor );

        // NOTE: Coordinate with "init" @BMRule above!
        final int threads = 4;
        final int timeoutSeconds = 10;

        final String content = "this is a test " + System.currentTimeMillis();
        final String base = "repo";
        final String path = "this/is/the/path.txt";

        final String baseurl = server.formatUrl( base );

        // Serve the content EXACTLY ONCE. It should be cached / cache should be used after that.
        server.expect( "GET", server.formatUrl( base, path ), new ExpectationHandler()
        {
            private boolean sent = false;

            @Override
            public void handle( final HttpServletRequest req,
                                final HttpServletResponse resp )
                    throws ServletException, IOException
            {
                if ( !sent )
                {
                    resp.setStatus( HttpServletResponse.SC_OK  );
                    resp.getWriter().write( content );
                    sent = true;
                }
                else
                {
                    throw new ServletException( "Cannot write content more than once." );
                }
            }
        } );

        final ConcreteResource resource = new ConcreteResource( new SimpleLocation( base, baseurl ), path );
        final Transfer target = cacheProvider.getTransfer( resource );


        final CountDownLatch latch = new CountDownLatch( threads );
        final AtomicInteger successes = new AtomicInteger( 0 );

        final Logger logger = LoggerFactory.getLogger( getClass() );

        // start threads, then wait for each to complete, and
        for( int i=0; i<threads; i++ )
        {
            executor.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    String oldName = Thread.currentThread().getName();
                    try
                    {
                        Thread.currentThread().setName( "Download - " + oldName );
                        Transfer result = handler.download( resource, target, timeoutSeconds, transport, false,
                                                              new EventMetadata() );

                        try(InputStream in = result.openInputStream())
                        {
                            String resultContent = IOUtils.toString( in );
                            if ( resultContent.equals( content ) )
                            {
                                successes.incrementAndGet();
                            }
                            else
                            {
                                logger.error( "Expected content: '{}'\nActual content: '{}'", content, resultContent );
                            }
                        }
                        catch ( IOException e )
                        {
                            logger.error( "Failed to read transfer: " + e.getMessage(), e );
                        }
                    }
                    catch ( TransferException e )
                    {
                        logger.error( "Failed to retrieve: " + e.getMessage(), e );
                    }
                    finally
                    {
                        latch.countDown();
                        Thread.currentThread().setName( oldName );
                    }
                }
            } );
        }
    }
}
