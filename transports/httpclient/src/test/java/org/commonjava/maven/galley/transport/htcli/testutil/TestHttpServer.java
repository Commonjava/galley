/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final int port;

    private Vertx vertx;

    private final String baseResource;

    private final Map<String, Integer> accessesByPath = new HashMap<String, Integer>();

    private final Map<String, String> errors = new HashMap<String, String>();

    public TestHttpServer( final String baseResource )
    {
        this.baseResource = baseResource;

        int port = -1;
        ServerSocket ss = null;
        for ( int i = 0; i < TRIES; i++ )
        {
            final int p = Math.abs( rand.nextInt() ) % 2000 + 8000;
            logger.info( "Trying port: {}", p );
            try
            {
                ss = new ServerSocket( p );
                port = p;
                break;
            }
            catch ( final IOException e )
            {
                logger.error( String.format( "Port %s failed. Reason: %s", p, e.getMessage() ), e );
            }
            finally
            {
                IOUtils.closeQuietly( ss );
            }
        }

        if ( port < 8000 )
        {
            throw new RuntimeException( "Failed to start test HTTP server. Cannot find open port in " + TRIES + " tries." );
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
        return String.format( "http://localhost:%s/%s/%s", port, baseResource, subpath );
    }

    public String getBaseUri()
    {
        return String.format( "http://localhost:%s/%s", port, baseResource );
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
            logger.error( "Returning registered error: {}", error );
            req.response()
               .setStatusCode( 500 )
               .setStatusMessage( error )
               .end();
            return;
        }

        logger.info( "Looking for classpath resource: '{}'", path );

        final URL url = Thread.currentThread()
                              .getContextClassLoader()
                              .getResource( path );

        logger.info( "Classpath URL is: '{}'", url );

        if ( url == null )
        {
            req.response()
               .setStatusCode( 404 )
               .setStatusMessage( "Not found" )
               .end();
        }
        else
        {
            final String method = req.method()
                                     .toUpperCase();

            logger.info( "Method: '{}'", method );
            if ( "GET".equals( method ) )
            {
                doGet( req, url );
            }
            else if ( "HEAD".equals( method ) )
            {
                req.response()
                   .setStatusCode( 200 )
                   .end();
            }
            else
            {
                req.response()
                   .setStatusCode( 400 )
                   .setStatusMessage( "Method: " + method + " not supported by test fixture." )
                   .end();
            }
        }
    }

    private void doGet( final HttpServerRequest req, final URL url )
    {
        InputStream stream = null;
        try
        {
            stream = url.openStream();

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy( stream, baos );

            final int len = baos.toByteArray().length;
            final Buffer buf = new Buffer( baos.toByteArray() );
            logger.info( "Send: {} bytes", len );
            req.response()
               .putHeader( "Content-Length", Integer.toString( len ) )
               .end( buf );
        }
        catch ( final IOException e )
        {
            logger.error( String.format( "Failed to stream content for: %s. Reason: %s", url, e.getMessage() ), e );
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

    public void registerException( final String url, final String error )
    {
        this.errors.put( url, error );
    }

}
