package org.commonjava.maven.galley.filearc;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.filearc.internal.ZipDownload;
import org.commonjava.maven.galley.filearc.internal.ZipListing;
import org.commonjava.maven.galley.filearc.internal.ZipPublish;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;

@ApplicationScoped
@Named( "zip-jar-galley-transport" )
public class ZipJarTransport
    implements Transport
{

    @Override
    public DownloadJob createDownloadJob( final String url, final Location repository, final Transfer target,
                                          final int timeoutSeconds )
        throws TransferException
    {
        return new ZipDownload( target );
    }

    @Override
    public PublishJob createPublishJob( final String url, final Location repository, final String path,
                                        final InputStream stream, final long length, final int timeoutSeconds )
        throws TransferException
    {
        return createPublishJob( url, repository, path, stream, length, null, timeoutSeconds );
    }

    @Override
    public PublishJob createPublishJob( final String url, final Location repository, final String path,
                                        final InputStream stream, final long length, final String contentType,
                                        final int timeoutSeconds )
        throws TransferException
    {
        return new ZipPublish( url, stream );
    }

    @Override
    public boolean handles( final Location location )
    {
        return ( location.getUri()
                         .startsWith( "zip:" ) && location.getUri()
                                                          .endsWith( ".zip" ) )
            || ( location.getUri()
                         .startsWith( "jar:" ) && location.getUri()
                                                          .endsWith( ".jar" ) );
    }

    @Override
    public ListingJob createListingJob( final Location repository, final String path, final int timeoutSeconds )
        throws TransferException
    {
        return new ZipListing( repository, path );
    }

}
