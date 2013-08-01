package org.commonjava.maven.galley.htcli.internal.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.commonjava.maven.galley.htcli.conf.GlobalHttpConfiguration;
import org.commonjava.maven.galley.htcli.conf.ProxyConfig;
import org.commonjava.maven.galley.htcli.model.HttpLocation;
import org.commonjava.maven.galley.model.Location;

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
    public int getTimeoutSeconds()
    {
        return delegate.getTimeoutSeconds();
    }

    @Override
    public String getKeyCertPem()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerCertPem()
    {
        // TODO Auto-generated method stub
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
        // FIXME: this could be 'user:password' presumably...
        return url.getUserInfo();
    }

    @Override
    public String getProxyHost()
    {
        final ProxyConfig proxy = globalConfig.getProxyConfig( url );
        return proxy == null ? null : proxy.getHost();
    }

    @Override
    public String getProxyUser()
    {
        final ProxyConfig proxy = globalConfig.getProxyConfig( url );
        return proxy == null ? null : proxy.getUser();
    }

    @Override
    public int getProxyPort()
    {
        final ProxyConfig proxy = globalConfig.getProxyConfig( url );
        return proxy == null ? 8080 : proxy.getPort();
    }

}
