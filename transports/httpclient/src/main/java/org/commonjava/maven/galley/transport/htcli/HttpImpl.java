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
package org.commonjava.maven.galley.transport.htcli;

import java.io.Closeable;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.commonjava.maven.galley.auth.PasswordEntry;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.transport.htcli.internal.CloseBlockingConnectionManager;
import org.commonjava.maven.galley.transport.htcli.internal.SSLUtils;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.maven.galley.transport.htcli.util.HttpUtil;
import org.commonjava.maven.galley.util.LocationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpImpl
    implements Http, Closeable
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final PasswordManager passwords;

    private final CloseBlockingConnectionManager connectionManager;

    public HttpImpl( final PasswordManager passwords )
    {
        this( passwords, 200 );
    }

    public HttpImpl( final PasswordManager passwords, final int maxConnections )
    {
        this.passwords = passwords;
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal( 200 );
        connectionManager = new CloseBlockingConnectionManager( cm );
    }

    public HttpImpl( final PasswordManager passwordManager, final HttpClientConnectionManager connectionManager )
    {
        passwords = passwordManager;
        this.connectionManager = new CloseBlockingConnectionManager( connectionManager );
    }

    @Override
    public CloseableHttpClient createClient()
        throws IOException
    {
        return createClient( null );
    }

    @Override
    public CloseableHttpClient createClient( final HttpLocation location )
        throws IOException
    {
        final HttpClientBuilder builder = HttpClients.custom()
                                                     .setConnectionManager( connectionManager );

        if ( location != null )
        {
            final LayeredConnectionSocketFactory sslFac = createSSLSocketFactory( location );
            if ( sslFac != null )
            {
                builder.setSSLSocketFactory( sslFac );
            }

            if ( location.getProxyHost() != null )
            {
                final HttpRoutePlanner planner =
                    new DefaultProxyRoutePlanner( new HttpHost( location.getProxyHost(), getProxyPort( location ) ) );
                builder.setRoutePlanner( planner );
            }

            final int timeout = 1000 * LocationUtils.getTimeoutSeconds( location );
            builder.setDefaultRequestConfig( RequestConfig.custom()
                                                          .setConnectionRequestTimeout( timeout )
                                                          .setSocketTimeout( timeout )
                                                          .setConnectTimeout( timeout )
                                                          .build() );
        }


        return builder.build();
    }

    private int getProxyPort( final HttpLocation location )
    {
        int port = location.getProxyPort();
        if ( port < 1 )
        {
            port = -1;
        }

        return port;
    }

    @Override
    public HttpClientContext createContext()
    {
        return createContext( null );
    }

    @Override
    public HttpClientContext createContext( final HttpLocation location )
    {
        final HttpClientContext ctx = HttpClientContext.create();

        if ( location != null )
        {
            final CredentialsProvider creds = new BasicCredentialsProvider();
            final AuthScope as = new AuthScope( location.getHost(), location.getPort() );

            if ( location.getUser() != null )
            {
                final String password =
                    passwords.getPassword( new PasswordEntry( location, PasswordEntry.USER_PASSWORD ) );
                creds.setCredentials( as, new UsernamePasswordCredentials( location.getUser(), password ) );
            }

            if ( location.getProxyHost() != null && location.getProxyUser() != null )
            {
                final String password =
                    passwords.getPassword( new PasswordEntry( location, PasswordEntry.PROXY_PASSWORD ) );
                creds.setCredentials( new AuthScope( location.getProxyHost(), getProxyPort( location ) ),
                                      new UsernamePasswordCredentials( location.getProxyUser(), password ) );
            }

            ctx.setCredentialsProvider( creds );
        }

        return ctx;
    }

    private SSLConnectionSocketFactory createSSLSocketFactory( final HttpLocation location )
        throws IOException
    {
        KeyStore ks = null;
        KeyStore ts = null;

        final String kcPem = location.getKeyCertPem();
        final String kcPass = passwords.getPassword( new PasswordEntry( location, PasswordEntry.KEY_PASSWORD ) );
        if ( kcPem != null )
        {
            if ( kcPass == null || kcPass.length() < 1 )
            {
                logger.error( "Invalid configuration. Location: {} cannot have an empty key password!",
                              location.getUri() );
                throw new IOException( "Location: " + location.getUri() + " is misconfigured!" );
            }

            try
            {
                ks = SSLUtils.readKeyAndCert( kcPem, kcPass );

                logger.debug( "Keystore contains the following certificates: {}", new CertEnumerator( ks ) );
            }
            catch ( final CertificateException e )
            {
                logger.error( String.format( "Invalid configuration. Location: %s has an invalid client certificate! Error: %s",
                                             location.getUri(), e.getMessage() ), e );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
            catch ( final KeyStoreException e )
            {
                logger.error( String.format( "Invalid configuration. Cannot initialize keystore for repository: %s. Error: %s",
                                             location.getUri(), e.getMessage() ), e );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
            catch ( final NoSuchAlgorithmException e )
            {
                logger.error( String.format( "Invalid configuration. Cannot initialize keystore for repository: %s. Error: %s",
                                             location.getUri(), e.getMessage() ), e );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
            catch ( final InvalidKeySpecException e )
            {
                logger.error( String.format( "Invalid configuration. Invalid client key for repository: %s. Error: %s",
                                             location.getUri(), e.getMessage() ), e );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
        }

        final String sPem = location.getServerCertPem();
        //        logger.debug( "Server certificate PEM:\n{}", sPem );
        if ( sPem != null )
        {
            try
            {
                ts = SSLUtils.readCerts( sPem, location.getHost() );

                //                logger.debug( "Trust store contains the following certificates:\n{}", new CertEnumerator( ts ) );
            }
            catch ( final CertificateException e )
            {
                logger.error( String.format( "Invalid configuration. Location: %s has an invalid server certificate! Error: %s",
                                             location.getUri(), e.getMessage() ), e );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
            catch ( final KeyStoreException e )
            {
                logger.error( String.format( "Invalid configuration. Cannot initialize keystore for repository: %s. Error: %s",
                                             location.getUri(), e.getMessage() ), e );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
            catch ( final NoSuchAlgorithmException e )
            {
                logger.error( String.format( "Invalid configuration. Cannot initialize keystore for repository: %s. Error: %s",
                                             location.getUri(), e.getMessage() ), e );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
        }

        if ( ks != null || ts != null )
        {
            try
            {
                SSLContextBuilder sslBuilder = SSLContexts.custom()
                                              .useProtocol( SSLConnectionSocketFactory.TLS );
                if ( ks != null )
                {
                    sslBuilder.loadKeyMaterial( ks, kcPass.toCharArray() );
                }

                if ( ts != null )
                {
                    sslBuilder.loadTrustMaterial( ts, null );
                }

                SSLContext ctx = sslBuilder.build();

                return new SSLConnectionSocketFactory( ctx, new DefaultHostnameVerifier() );
            }
            catch ( final KeyManagementException e )
            {
                logger.error( "Invalid configuration. Cannot initialize SSL socket factory for repository: {}. Error: {}",
                              e, location.getUri(), e.getMessage() );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
            catch ( final UnrecoverableKeyException e )
            {
                logger.error( "Invalid configuration. Cannot initialize SSL socket factory for repository: {}. Error: {}",
                              e, location.getUri(), e.getMessage() );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
            catch ( final NoSuchAlgorithmException e )
            {
                logger.error( "Invalid configuration. Cannot initialize SSL socket factory for repository: {}. Error: {}",
                              e, location.getUri(), e.getMessage() );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
            catch ( final KeyStoreException e )
            {
                logger.error( "Invalid configuration. Cannot initialize SSL socket factory for repository: {}. Error: {}",
                              e, location.getUri(), e.getMessage() );
                throw new IOException( "Failed to initialize SSL connection for repository: " + location.getUri() );
            }
        }

        return null;
    }

    @Override
    public void cleanup( final CloseableHttpClient client, final HttpUriRequest request,
                         final CloseableHttpResponse response )
    {
        HttpUtil.cleanupResources( client, request, response );
    }

    @Override
    public void close()
        throws IOException
    {
        connectionManager.reallyShutdown();
    }
}
