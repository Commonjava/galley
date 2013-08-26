package org.commonjava.maven.galley.transport.htcli.testutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.commonjava.util.logging.Logger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.impl.DefaultVertx;

public class TestHttpServer
    implements Handler<HttpServerRequest>
{

    private static final int TRIES = 4;

    private static Random rand = new Random();

    private final Logger logger = new Logger( getClass() );

    private final int port;

    private Vertx vertx;

    private final String baseResource;

    private final Map<String, Integer> accessesByPath = new HashMap<>();

    private final Map<String, String> errors = new HashMap<>();

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
    }

    public int getPort()
    {
        return port;
    }

    public void shutdown()
    {
        if ( vertx != null )
        {
            vertx.stop();
        }
    }

    public void start()
        throws Exception
    {
        vertx = new DefaultVertx();
        vertx.createHttpServer()
             .requestHandler( this )
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

    public Map<String, Integer> getAccessesByPath()
    {
        return accessesByPath;
    }

    public Map<String, String> getRegisteredErrors()
    {
        return errors;
    }

    @Override
    public void handle( final HttpServerRequest req )
    {
        final String wholePath = req.path();
        String path = wholePath;
        if ( path.length() > 1 )
        {
            path = path.substring( 1 );
        }

        final Integer i = accessesByPath.get( wholePath );
        if ( i == null )
        {
            accessesByPath.put( wholePath, 1 );
        }
        else
        {
            accessesByPath.put( wholePath, i + 1 );
        }

        if ( errors.containsKey( wholePath ) )
        {
            final String error = errors.get( wholePath );
            logger.error( "Returning registered error: %s", error );
            req.response()
               .setStatusCode( 500 )
               .setStatusMessage( error )
               .end();
            return;
        }

        logger.info( "Looking for classpath resource: '%s'", path );

        final URL url = Thread.currentThread()
                              .getContextClassLoader()
                              .getResource( path );

        logger.info( "Classpath URL is: '%s'", url );

        if ( url == null )
        {
            req.response()
               .setStatusCode( 404 )
               .setStatusMessage( "Not found" )
               .end();
        }
        else
        {
            InputStream stream = null;
            try
            {
                stream = url.openStream();

                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy( stream, baos );

                final int len = baos.toByteArray().length;
                final Buffer buf = new Buffer( baos.toByteArray() );
                logger.info( "Send: %d bytes", len );
                req.response()
                   .putHeader( "Content-Length", Integer.toString( len ) )
                   .end( buf );
            }
            catch ( final IOException e )
            {
                logger.error( "Failed to stream content for: %s. Reason: %s", e, url, e.getMessage() );
                req.response()
                   .setStatusCode( 500 )
                   .setStatusMessage( "FAIL: " + e.getMessage() )
                   .end();
            }
            finally
            {
                IOUtils.closeQuietly( stream );
            }
        }
    }

    public void registerException( final String url, final String error )
    {
        this.errors.put( url, error );
    }

}
