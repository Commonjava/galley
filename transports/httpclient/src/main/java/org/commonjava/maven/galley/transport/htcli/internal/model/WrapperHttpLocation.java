/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.transport.htcli.internal.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.commonjava.maven.galley.model.Attributes;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.commonjava.maven.galley.transport.htcli.conf.ProxyConfig;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

public class WrapperHttpLocation extends Attributes
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
    public String getHost()
    {
        return url.getHost();
    }

    @Override
    public int getPort()
    {
        return url.getPort();
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
    public <T> T getAttribute( final String key, final Class<T> type )
    {
        return delegate.getAttribute( key, type );
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
    public String getName()
    {
        return delegate.getName();
    }

}
