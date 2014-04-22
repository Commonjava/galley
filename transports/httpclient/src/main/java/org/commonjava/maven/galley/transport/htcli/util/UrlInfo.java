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
