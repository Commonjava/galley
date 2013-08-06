package org.commonjava.maven.galley.transport.htcli.internal.model;

import java.net.MalformedURLException;

import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.maven.galley.transport.htcli.util.UrlInfo;

public class SimpleHttpLocation
    extends SimpleLocation
    implements HttpLocation
{

    private String keyCertPem;

    private String serverCertPem;

    private final UrlInfo urlInfo;

    private final UrlInfo proxyUrlInfo;

    public SimpleHttpLocation( final String uri, final boolean allowSnapshots, final boolean allowReleases,
                               final boolean allowsStoring, final boolean allowPublishing, final int timeoutSeconds,
                               final String proxyUri )
        throws MalformedURLException
    {
        super( uri, allowSnapshots, allowReleases, allowsStoring, allowPublishing, timeoutSeconds );
        this.urlInfo = new UrlInfo( uri );
        this.proxyUrlInfo = proxyUri == null ? null : new UrlInfo( proxyUri, 8080 );
    }

    @Override
    public String getKeyCertPem()
    {
        return keyCertPem;
    }

    @Override
    public String getServerCertPem()
    {
        return serverCertPem;
    }

    @Override
    public String getHost()
    {
        return urlInfo.getHost();
    }

    @Override
    public int getPort()
    {
        return urlInfo.getPort();
    }

    @Override
    public String getUser()
    {
        return urlInfo.getUser();
    }

    @Override
    public String getProxyHost()
    {
        return proxyUrlInfo == null ? null : proxyUrlInfo.getHost();
    }

    @Override
    public String getProxyUser()
    {
        return proxyUrlInfo == null ? null : proxyUrlInfo.getUser();
    }

    @Override
    public int getProxyPort()
    {
        return proxyUrlInfo == null ? -1 : proxyUrlInfo.getPort();
    }

}
