/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley.transport.htcli.util;

import java.net.MalformedURLException;
import java.net.URL;

public final class UrlInfo
{
    private final String url;

    private String user;

    private String password;

    private final String host;

    private int port;

    private final String rawUrl;

    public UrlInfo( final String u )
        throws MalformedURLException
    {
        this( u, -1 );
    }

    public UrlInfo( final String u, final int defaultPort )
        throws MalformedURLException
    {
        this.rawUrl = u;
        String resultUrl = u;

        final URL url = new URL( u );

        final String userInfo = url.getUserInfo();
        if ( userInfo != null && user == null && password == null )
        {
            user = userInfo;
            password = null;

            int idx = userInfo.indexOf( ':' );
            if ( idx > 0 )
            {
                user = userInfo.substring( 0, idx );
                password = userInfo.substring( idx + 1 );

                final StringBuilder sb = new StringBuilder();
                idx = this.url.indexOf( "://" );
                sb.append( this.url.substring( 0, idx + 3 ) );

                idx = this.url.indexOf( "@" );
                if ( idx > 0 )
                {
                    sb.append( this.url.substring( idx + 1 ) );
                }

                resultUrl = sb.toString();
            }
        }

        this.url = resultUrl;

        host = url.getHost();
        if ( url.getPort() < 0 )
        {
            if ( defaultPort > 0 )
            {
                port = defaultPort;
            }
            else
            {
                port = url.getProtocol()
                          .equals( "https" ) ? 443 : 80;
            }
        }
        else
        {
            port = url.getPort();
        }
    }

    public String getRawUrl()
    {
        return rawUrl;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUser()
    {
        return user;
    }

    public String getPassword()
    {
        return password;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

}
