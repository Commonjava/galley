/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.transport.htcli;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpProtocolParams;
import org.commonjava.maven.atlas.ident.util.StringFormat;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.transport.htcli.internal.LocationSSLSocketFactory;
import org.commonjava.maven.galley.transport.htcli.internal.TLLocationCredentialsProvider;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpImpl
    implements Http
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private LocationSSLSocketFactory socketFactory;

    private TLLocationCredentialsProvider credProvider;

    private DefaultHttpClient client;

    private final PasswordManager passwords;

    public HttpImpl( final PasswordManager passwords )
    {
        this( passwords, 20 );
    }

    public HttpImpl( final PasswordManager passwords, final int maxConnections )
    {
        this.passwords = passwords;
        setup();
    }

    protected void setup()
    {
        final PoolingClientConnectionManager ccm = new PoolingClientConnectionManager();

        // TODO: Make this configurable
        ccm.setMaxTotal( 20 );

        credProvider = new TLLocationCredentialsProvider( passwords );

        try
        {
            socketFactory = new LocationSSLSocketFactory( passwords, credProvider );

            final SchemeRegistry registry = ccm.getSchemeRegistry();
            registry.register( new Scheme( "https", 443, socketFactory ) );
        }
        catch ( final KeyManagementException e )
        {
            logger.error( "{}", e, new StringFormat( "Failed to setup SSLSocketFactory. SSL mutual authentication will not be available!\nError: {}", e.getMessage() ) );
        }
        catch ( final UnrecoverableKeyException e )
        {
            logger.error( "{}", e, new StringFormat( "Failed to setup SSLSocketFactory. SSL mutual authentication will not be available!\nError: {}", e.getMessage() ) );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            logger.error( "{}", e, new StringFormat( "Failed to setup SSLSocketFactory. SSL mutual authentication will not be available!\nError: {}", e.getMessage() ) );
        }
        catch ( final KeyStoreException e )
        {
            logger.error( "{}", e, new StringFormat( "Failed to setup SSLSocketFactory. SSL mutual authentication will not be available!\nError: {}", e.getMessage() ) );
        }

        final DefaultHttpClient hc = new DefaultHttpClient( ccm );
        hc.setCredentialsProvider( credProvider );

        HttpProtocolParams.setVersion( hc.getParams(), HttpVersion.HTTP_1_1 );

        client = hc;
    }

    @Override
    public HttpClient getClient()
    {
        return client;
    }

    @Override
    public void bindCredentialsTo( final HttpLocation location, final HttpRequest request )
    {
        credProvider.bind( location );

        if ( location.getProxyHost() != null )
        {
            //            logger.info( "Using proxy: {}:{} for repository: {}", repository.getProxyHost(),
            //                         repository.getProxyPort() < 1 ? 80 : repository.getProxyPort(), repository.getName() );

            final int proxyPort = location.getProxyPort();
            HttpHost proxy;
            if ( proxyPort < 1 )
            {
                proxy = new HttpHost( location.getProxyHost(), -1, "http" );
            }
            else
            {
                proxy = new HttpHost( location.getProxyHost(), location.getProxyPort(), "http" );
            }

            request.getParams()
                   .setParameter( ConnRoutePNames.DEFAULT_PROXY, proxy );
        }

        request.getParams()
               .setParameter( Http.HTTP_PARAM_LOCATION, location );
    }

    @Override
    public void closeConnection()
    {
        client.getConnectionManager()
              .closeExpiredConnections();

        client.getConnectionManager()
              .closeIdleConnections( 2, TimeUnit.SECONDS );
    }

    @Override
    public void clearBoundCredentials( final HttpLocation location )
    {
        credProvider.clear();
    }

    @Override
    public void clearAllBoundCredentials()
    {
        credProvider.clear();
    }

}
