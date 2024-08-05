/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
package org.commonjava.maven.galley.transport.htcli.internal.model;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.commonjava.maven.galley.transport.htcli.conf.HttpJobType;
import org.commonjava.maven.galley.transport.htcli.conf.ProxyConfig;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.maven.galley.transport.htcli.model.LocationTrustType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class WrapperHttpLocation
        implements HttpLocation
{

    private final Location delegate;

    private final URL url;

    private final GlobalHttpConfiguration globalConfig;

    private final HttpJobType httpJobType;

    public WrapperHttpLocation( final Location delegate, final GlobalHttpConfiguration globalConfig,
                                final HttpJobType httpJobType )
            throws MalformedURLException
    {
        this.delegate = delegate;
        this.globalConfig = globalConfig;
        this.url = new URL( delegate.getUri() );
        this.httpJobType = httpJobType;
    }

    @Override
    public boolean allowsSnapshots()
    {
        return delegate.allowsSnapshots();
    }

    @Override
    public boolean allowsReleases()
    {
        return delegate.allowsReleases();
    }

    @Override
    public String getUri()
    {
        return delegate.getUri();
    }

    @Override
    public String getKeyCertPem()
    {
        return delegate instanceof HttpLocation ? ( (HttpLocation) delegate ).getKeyCertPem() : null;
    }

    @Override
    public String getServerCertPem()
    {
        return delegate instanceof HttpLocation ? ( (HttpLocation) delegate ).getServerCertPem() : null;
    }

    @Override
    public LocationTrustType getTrustType()
    {
        // FIXME: Is this something we should handle in the global config?
        return delegate instanceof HttpLocation ? ( (HttpLocation) delegate ).getTrustType() : null;
    }

    @Override
    public String getHost()
    {
        return delegate instanceof HttpLocation ? ( (HttpLocation) delegate ).getHost() : url.getHost();
    }

    @Override
    public int getPort()
    {
        int port = url.getPort() < 0 ? url.getDefaultPort() : url.getPort();
        return delegate instanceof HttpLocation ? ( (HttpLocation) delegate ).getPort() : port;
    }

    @Override
    public String getUser()
    {
        final String userpass = url.getUserInfo();
        if ( userpass != null )
        {
            final int idx = userpass.indexOf( ":" );
            if ( idx > -1 )
            {
                return userpass.substring( 0, idx );
            }
        }

        return delegate instanceof HttpLocation ? ( (HttpLocation) delegate ).getUser() : userpass;
    }

    @Override
    public String getProxyHost()
    {
        ProxyConfig proxy = getProxyConfig();
        return isProxyAllowHttpJobType( proxy ) ? proxy.getHost() : null;
    }

    @Override
    public String getProxyUser()
    {
        ProxyConfig proxy = getProxyConfig();
        return isProxyAllowHttpJobType( proxy ) ? proxy.getUser() : null;
    }

    @Override
    public int getProxyPort()
    {
        ProxyConfig proxy = getProxyConfig();
        return isProxyAllowHttpJobType( proxy ) ? proxy.getPort() : 8080;
    }

    @Override
    public boolean isIgnoreHostnameVerification()
    {
        return delegate instanceof HttpLocation && ( (HttpLocation) delegate ).isIgnoreHostnameVerification();
    }

    @SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" )
    @Override
    public boolean equals( final Object other )
    {
        return delegate.equals( other );
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode();
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        return delegate.getAttributes();
    }

    @Override
    public <T> T getAttribute( final String key, final Class<T> type )
    {
        return delegate.getAttribute( key, type );
    }

    @Override
    public <T> T getAttribute( final String key, final Class<T> type, final T defaultValue )
    {
        return delegate.getAttribute( key, type, defaultValue );
    }

    @Override
    public Object removeAttribute( final String key )
    {
        return delegate.removeAttribute( key );
    }

    @Override
    public Object setAttribute( final String key, final Object value )
    {
        return delegate.setAttribute( key, value );
    }

    @Override
    public boolean allowsPublishing()
    {
        return delegate.allowsPublishing();
    }

    @Override
    public boolean allowsStoring()
    {
        return delegate.allowsStoring();
    }

    @Override
    public boolean allowsDownloading()
    {
        return true;
    }

    @Override
    public boolean allowsDeletion()
    {
        return delegate.allowsDeletion();
    }

    @Override
    public String getName()
    {
        return delegate.getName();
    }

    private ProxyConfig getProxyConfig()
    {
        return globalConfig == null ? null : globalConfig.getProxyConfig();
    }

    private boolean isProxyAllowHttpJobType( ProxyConfig proxy )
    {
        return proxy != null && proxy.getAllowHttpJobTypes().contains( httpJobType.name() );
    }
}
