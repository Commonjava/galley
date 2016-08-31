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

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.commonjava.maven.galley.GalleyException;
import org.commonjava.maven.galley.TransferLocationException;
import org.commonjava.maven.galley.auth.PasswordEntry;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.transport.htcli.internal.util.HttpFactoryPasswordDelegate;
import org.commonjava.maven.galley.transport.htcli.internal.util.LocationLookup;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.maven.galley.transport.htcli.util.HttpUtil;
import org.commonjava.maven.galley.util.LocationUtils;
import org.commonjava.util.jhttpc.HttpFactory;
import org.commonjava.util.jhttpc.JHttpCException;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.commonjava.util.jhttpc.model.SiteConfigBuilder;
import org.commonjava.util.jhttpc.model.SiteTrustType;

import javax.enterprise.context.ApplicationScoped;
import java.io.Closeable;
import java.io.IOException;

@ApplicationScoped
public class HttpImpl
        implements Http, Closeable
{
    private final PasswordManager passwords;

    private final HttpFactory httpFactory;

    private final LocationLookup locationLookup;

    public HttpImpl( final PasswordManager passwords )
    {
        this.passwords = passwords;
        this.locationLookup = new LocationLookup();
        this.httpFactory = new HttpFactory( new HttpFactoryPasswordDelegate( passwords, locationLookup ) );
    }

    @Override
    public CloseableHttpClient createClient()
            throws GalleyException
    {
        return createClient( null );
    }

    @Override
    public CloseableHttpClient createClient( final HttpLocation location )
            throws GalleyException
    {
        try
        {
            if ( location != null )
            {
                locationLookup.register( location );

                int maxConnections = LocationUtils.getMaxConnections( location );
                SiteConfigBuilder configBuilder = new SiteConfigBuilder( location.getName(), location.getUri() );
                configBuilder.withAttributes( location.getAttributes() )
                             .withKeyCertPem( location.getKeyCertPem() )
                             .withServerCertPem( location.getServerCertPem() )
                             .withProxyHost( location.getProxyHost() )
                             .withProxyPort( location.getProxyPort() )
                             .withProxyUser( location.getProxyUser() )
                             .withRequestTimeoutSeconds( LocationUtils.getTimeoutSeconds( location ) )
                             .withUser( location.getUser() )
                             .withMaxConnections( maxConnections );

                if ( location.getTrustType() != null )
                {
                    configBuilder.withTrustType( SiteTrustType.getType( location.getTrustType().name() ) );
                }


                SiteConfig config = configBuilder.build();

                return httpFactory.createClient( config );
            }
            else
            {
                return httpFactory.createClient();
            }
        }
        catch ( JHttpCException e )
        {
            throw new TransferLocationException( location, "Failed to initialize http client: %s", e, e.getMessage() );
        }
        finally{}
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
    }
}
