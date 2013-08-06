package org.commonjava.maven.galley.transport.htcli.internal;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.util.logging.Logger;

public class HttpImpl
    implements Http
{
    private final Logger logger = new Logger( getClass() );

    private LocationSSLSocketFactory socketFactory;

    private final TLLocationCredentialsProvider credProvider;

    private final DefaultHttpClient client;

    public HttpImpl( final PasswordManager passwords )
    {
        final PoolingClientConnectionManager ccm = new PoolingClientConnectionManager();

        // TODO: Make this configurable
        ccm.setMaxTotal( 20 );

        credProvider = new TLLocationCredentialsProvider( passwords );

        try
        {
            socketFactory = new LocationSSLSocketFactory( passwords, credProvider );

            final SchemeRegistry registry = ccm.getSchemeRegistry();
            registry.register( new Scheme( "https", 443, socketFactory ) );
        }
        catch ( final KeyManagementException e )
        {
            logger.error( "Failed to setup SSLSocketFactory. SSL mutual authentication will not be available!\nError: %s",
                          e, e.getMessage() );
        }
        catch ( final UnrecoverableKeyException e )
        {
            logger.error( "Failed to setup SSLSocketFactory. SSL mutual authentication will not be available!\nError: %s",
                          e, e.getMessage() );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            logger.error( "Failed to setup SSLSocketFactory. SSL mutual authentication will not be available!\nError: %s",
                          e, e.getMessage() );
        }
        catch ( final KeyStoreException e )
        {
            logger.error( "Failed to setup SSLSocketFactory. SSL mutual authentication will not be available!\nError: %s",
                          e, e.getMessage() );
        }

        final DefaultHttpClient hc = new DefaultHttpClient( ccm );
        hc.setCredentialsProvider( credProvider );

        client = hc;
    }

    @Override
    public HttpClient getClient()
    {
        return client;
    }

    @Override
    public void bindCredentialsTo( final HttpLocation location, final HttpRequest request )
    {
        credProvider.bind( location );

        if ( location.getProxyHost() != null )
        {
            //            logger.info( "Using proxy: %s:%s for repository: %s", repository.getProxyHost(),
            //                         repository.getProxyPort() < 1 ? 80 : repository.getProxyPort(), repository.getName() );

            final int proxyPort = location.getProxyPort();
            HttpHost proxy;
            if ( proxyPort < 1 )
            {
                proxy = new HttpHost( location.getProxyHost(), -1, "http" );
            }
            else
            {
                proxy = new HttpHost( location.getProxyHost(), location.getProxyPort(), "http" );
            }

            request.getParams()
                   .setParameter( ConnRoutePNames.DEFAULT_PROXY, proxy );
        }

        request.getParams()
               .setParameter( Http.HTTP_PARAM_LOCATION, location );
    }

    @Override
    public void closeConnection()
    {
        client.getConnectionManager()
              .closeExpiredConnections();

        client.getConnectionManager()
              .closeIdleConnections( 2, TimeUnit.SECONDS );
    }

    @Override
    public void clearBoundCredentials( final HttpLocation location )
    {
        credProvider.clear();
    }

    @Override
    public void clearAllBoundCredentials()
    {
        credProvider.clear();
    }

}
