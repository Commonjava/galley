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
