package org.commonjava.maven.galley.transport.htcli.testutil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.commonjava.util.logging.Logger;
import org.junit.rules.ExternalResource;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultVertx;

public class TestHttpServer
    extends ExternalResource
{

    private static final int TRIES = 4;

    private static Random rand = new Random();

    private final Logger logger = new Logger( getClass() );

    private final int port;

    private Vertx vertx;

    private final ClasspathHandler handler;

    private final String baseResource;

    public TestHttpServer( final String baseResource )
    {
        this.baseResource = baseResource;

        int port = -1;
        ServerSocket ss = null;
        for ( int i = 0; i < TRIES; i++ )
        {
            final int p = Math.abs( rand.nextInt() ) % 2000 + 8000;
            logger.info( "Trying port: %s", p );
            try
            {
                ss = new ServerSocket( p );
                port = p;
                break;
            }
            catch ( final IOException e )
            {
                logger.error( "Port %s failed. Reason: %s", e, p, e.getMessage() );
            }
            finally
            {
                IOUtils.closeQuietly( ss );
            }
        }

        if ( port < 8000 )
        {
            throw new RuntimeException( "Failed to start test HTTP server. Cannot find open port in " + TRIES
                + " tries." );
        }

        this.port = port;
        this.handler = new ClasspathHandler();
    }

    public void registerException( final String url, final String error )
    {
        this.handler.registerException( url, error );
    }

    public int getPort()
    {
        return port;
    }

    @Override
    protected void after()
    {
        if ( vertx != null )
        {
            vertx.stop();
        }

        super.after();
    }

    public Map<String, Integer> getAccessesByPath()
    {
        return handler.getAccessesByPath();
    }

    public Map<String, String> getRegisteredErrors()
    {
        return handler.getRegisteredErrors();
    }

    @Override
    protected void before()
        throws Throwable
    {
        super.before();

        vertx = new DefaultVertx();
        vertx.createHttpServer()
             .requestHandler( handler )
             .listen( port );
    }

    public String formatUrl( final String subpath )
    {
        return String.format( "http://localhost:%d/%s/%s", port, baseResource, subpath );
    }

    public String getBaseUri()
    {
        return String.format( "http://localhost:%d/%s", port, baseResource );
    }

    public String getUrlPath( final String url )
        throws MalformedURLException
    {
        return new URL( url ).getPath();
    }

}
