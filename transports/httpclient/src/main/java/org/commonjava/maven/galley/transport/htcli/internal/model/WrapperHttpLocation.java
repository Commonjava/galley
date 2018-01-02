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
package org.commonjava.maven.galley.transport.htcli.internal.model;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
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

    public WrapperHttpLocation( final Location delegate, final GlobalHttpConfiguration globalConfig )
        throws MalformedURLException
    {
        this.delegate = delegate;
        this.globalConfig = globalConfig;
        this.url = new URL( delegate.getUri() );
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
        return null;
    }

    @Override
    public String getServerCertPem()
    {
        return null;
    }

    @Override
    public LocationTrustType getTrustType()
    {
        // FIXME: Is this something we should handle in the global config?
        return null;
    }

    @Override
    public String getHost()
    {
        return url.getHost();
    }

    @Override
    public int getPort()
    {
        return url.getPort() < 0 ? url.getDefaultPort() : url.getPort();
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

        return userpass;
    }

    @Override
    public String getProxyHost()
    {
        final ProxyConfig proxy = globalConfig == null ? null : globalConfig.getProxyConfig( url );
        return proxy == null ? null : proxy.getHost();
    }

    @Override
    public String getProxyUser()
    {
        final ProxyConfig proxy = globalConfig == null ? null : globalConfig.getProxyConfig( url );
        return proxy == null ? null : proxy.getUser();
    }

    @Override
    public int getProxyPort()
    {
        final ProxyConfig proxy = globalConfig == null ? null : globalConfig.getProxyConfig( url );
        return proxy == null ? 8080 : proxy.getPort();
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

}
