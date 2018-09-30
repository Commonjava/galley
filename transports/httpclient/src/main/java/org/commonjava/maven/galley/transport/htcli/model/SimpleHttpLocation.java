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
package org.commonjava.maven.galley.transport.htcli.model;

import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.transport.htcli.util.UrlInfo;

import java.net.MalformedURLException;

public class SimpleHttpLocation
    extends SimpleLocation
    implements HttpLocation
{

    private String keyCertPem;

    private String serverCertPem;

    private final UrlInfo urlInfo;

    private final UrlInfo proxyUrlInfo;

    private LocationTrustType trustType;

    public SimpleHttpLocation( final String name, final String uri, final boolean allowSnapshots,
                               final boolean allowReleases, final boolean allowsStoring, final boolean allowPublishing,
                               final boolean allowDeletion, final String proxyUri )
            throws MalformedURLException
    {
        super( name, uri, allowSnapshots, allowReleases, allowsStoring, allowPublishing, true, allowDeletion );
        this.urlInfo = new UrlInfo( uri );
        this.proxyUrlInfo = proxyUri == null ? null : new UrlInfo( proxyUri, 8080 );
    }

    public SimpleHttpLocation( final String name, final String uri, final boolean allowSnapshots,
                               final boolean allowReleases, final boolean allowsStoring, final boolean allowPublishing,
                               final String proxyUri ) throws MalformedURLException
    {
        super( name, uri, allowSnapshots, allowReleases, allowsStoring, allowPublishing, true, true );
        this.urlInfo = new UrlInfo( uri );
        this.proxyUrlInfo = proxyUri == null ? null : new UrlInfo( proxyUri, 8080 );
    }

    public SimpleHttpLocation( final String name, final String uri, final boolean allowSnapshots,
                               final boolean allowReleases, final boolean allowsStoring, final boolean allowPublishing,
                               final boolean allowDeletion, final String proxyUri, LocationTrustType trustType )
        throws MalformedURLException
    {
        super( name, uri, allowSnapshots, allowReleases, allowsStoring, allowPublishing, true, allowDeletion );
        this.trustType = trustType;
        this.urlInfo = new UrlInfo( uri );
        this.proxyUrlInfo = proxyUri == null ? null : new UrlInfo( proxyUri, 8080 );
    }

    public SimpleHttpLocation( final String name, final String uri, final boolean allowSnapshots,
                               final boolean allowReleases, final boolean allowsStoring, final boolean allowPublishing,
                               final String proxyUri, LocationTrustType trustType ) throws MalformedURLException
    {
        super( name, uri, allowSnapshots, allowReleases, allowsStoring, allowPublishing, true, true );
        this.trustType = trustType;
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
    public LocationTrustType getTrustType()
    {
        return trustType;
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

    @Override
    public boolean isIgnoreHostnameVerification()
    {
        return false;
    }

}
