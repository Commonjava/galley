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
package org.commonjava.maven.galley.transport.htcli.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.commonjava.maven.galley.auth.PasswordEntry;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

public class TLLocationCredentialsProvider
    implements CredentialsProvider
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final ThreadLocal<Map<AuthScope, Credentials>> credentials = new ThreadLocal<Map<AuthScope, Credentials>>();

    private final ThreadLocal<Map<AuthScope, HttpLocation>> repositories = new ThreadLocal<Map<AuthScope, HttpLocation>>();

    private final PasswordManager passwordManager;

    public TLLocationCredentialsProvider( final PasswordManager passwordManager )
    {
        this.passwordManager = passwordManager;
    }

    public synchronized void bind( final Collection<HttpLocation> locations )
    {
        if ( locations != null )
        {
            final Map<AuthScope, Credentials> creds = new HashMap<AuthScope, Credentials>();
            final Map<AuthScope, HttpLocation> repos = new HashMap<AuthScope, HttpLocation>();
            for ( final HttpLocation location : locations )
            {
                final AuthScope as = new AuthScope( location.getHost(), location.getPort() );
                //                logger.info( "Storing repository def: {} under authscope: {}:{}", repository.getName(),
                //                             repository.getHost(), repository.getPort() );

                //TODO: Seems like multiple repos with same host/port could easily cause confusion if they're not configured the same way later on...
                repos.put( as, location );

                if ( location.getUser() != null )
                {
                    creds.put( as,
                               new UsernamePasswordCredentials(
                                                                location.getUser(),
                                                                passwordManager.getPassword( new PasswordEntry( location, PasswordEntry.USER_PASSWORD ) ) ) );
                }

                if ( location.getProxyHost() != null && location.getProxyUser() != null )
                {
                    creds.put( new AuthScope( location.getProxyHost(), location.getProxyPort() ),
                               new UsernamePasswordCredentials( location.getProxyUser(),
                                                                passwordManager.getPassword( new PasswordEntry( location,
                                                                                                                PasswordEntry.PROXY_PASSWORD ) ) ) );
                }
            }

            this.credentials.set( creds );
            this.repositories.set( repos );
        }
    }

    public HttpLocation getLocation( final String host, final int port )
    {
        //        logger.info( "Looking up repository def under authscope: {}:{}", host, port );

        final Map<AuthScope, HttpLocation> repos = repositories.get();
        if ( repos == null )
        {
            return null;
        }

        //TODO: Seems like multiple repos with same host/port could easily cause confusion if they're not configured the same way later on...
        return repos.get( new AuthScope( host, port ) );
    }

    public void bind( final HttpLocation... locations )
    {
        bind( Arrays.asList( locations ) );
    }

    @Override
    public void clear()
    {
        credentials.set( null );
    }

    @Override
    public synchronized void setCredentials( final AuthScope authscope, final Credentials creds )
    {
        Map<AuthScope, Credentials> map = credentials.get();
        if ( map == null )
        {
            map = new HashMap<AuthScope, Credentials>();
            credentials.set( map );
        }
        map.put( authscope, creds );
    }

    @Override
    public Credentials getCredentials( final AuthScope authscope )
    {
        final Map<AuthScope, Credentials> map = credentials.get();
        return map.get( authscope );
    }

}
