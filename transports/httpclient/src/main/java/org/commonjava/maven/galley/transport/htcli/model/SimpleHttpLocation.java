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
package org.commonjava.maven.galley.transport.htcli.model;

import java.net.MalformedURLException;

import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.transport.htcli.util.UrlInfo;

public class SimpleHttpLocation
    extends SimpleLocation
    implements HttpLocation
{

    private String keyCertPem;

    private String serverCertPem;

    private final UrlInfo urlInfo;

    private final UrlInfo proxyUrlInfo;

    public SimpleHttpLocation( final String name, final String uri, final boolean allowSnapshots,
                               final boolean allowReleases, final boolean allowsStoring, final boolean allowPublishing,
                               final int timeoutSeconds, final String proxyUri )
        throws MalformedURLException
    {
        super( name, uri, allowSnapshots, allowReleases, allowsStoring, allowPublishing, true, timeoutSeconds );
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
