package org.commonjava.maven.galley.transport.htcli;

import java.io.InputStream;
import java.net.MalformedURLException;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.commonjava.maven.galley.transport.htcli.internal.HttpDownload;
import org.commonjava.maven.galley.transport.htcli.internal.HttpPublish;
import org.commonjava.maven.galley.transport.htcli.internal.model.WrapperHttpLocation;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

public class HttpClientTransport
    implements Transport
{

    private final Http http;

    private final GlobalHttpConfiguration globalConfig;

    public HttpClientTransport( final Http http, final GlobalHttpConfiguration globalConfig )
    {
        this.http = http;
        this.globalConfig = globalConfig;
    }

    @Override
    public DownloadJob createDownloadJob( final String url, final Location repository, final Transfer target,
                                          final int timeoutSeconds )
        throws TransferException
    {
        final HttpLocation hl;
        try
        {
            hl =
                ( repository instanceof HttpLocation ) ? (HttpLocation) repository
                                : new WrapperHttpLocation( repository, globalConfig );
        }
        catch ( final MalformedURLException e )
        {
            throw new TransferException( "Failed to parse base-URL for: %s", e, repository.getUri() );
        }

        return new HttpDownload( url, hl, target, http );
    }

    @Override
    public PublishJob createPublishJob( final String url, final Location repository, final String path,
                                        final InputStream stream, final long length, final String contentType,
                                        final int timeoutSeconds )
        throws TransferException
    {
        final HttpLocation hl;
        try
        {
            hl =
                ( repository instanceof HttpLocation ) ? (HttpLocation) repository
                                : new WrapperHttpLocation( repository, globalConfig );
        }
        catch ( final MalformedURLException e )
        {
            throw new TransferException( "Failed to parse base-URL for: %s", e, repository.getUri() );
        }

        return new HttpPublish( url, hl, stream, length, contentType, http );
    }

    @Override
    public PublishJob createPublishJob( final String url, final Location repository, final String path,
                                        final InputStream stream, final long length, final int timeoutSeconds )
        throws TransferException
    {
        return createPublishJob( url, repository, path, stream, length, null, timeoutSeconds );
    }

}
