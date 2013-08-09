package org.commonjava.maven.galley.filearc;

import java.io.File;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.filearc.internal.FileDownload;
import org.commonjava.maven.galley.filearc.internal.FilePublish;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;

@ApplicationScoped
@Named( "file-galley-transport" )
public class FileTransport
    implements Transport
{

    @Inject
    private FileTransportConfig config;

    public FileTransport()
    {
    }

    public FileTransport( final File pubDir, final PathGenerator generator )
    {
        this.config = new FileTransportConfig( pubDir, generator );
    }

    public FileTransport( final FileTransportConfig config )
    {
        this.config = config;
    }

    @Override
    public DownloadJob createDownloadJob( final String url, final Location repository, final Transfer target,
                                          final int timeoutSeconds )
        throws TransferException
    {
        final File src = new File( url );
        return new FileDownload( target, src );
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
        final File pubDir = config.getPubDir();
        if ( pubDir == null )
        {
            throw new TransferException( "This transport is read-only!" );
        }

        final File dest = new File( pubDir, config.getGenerator()
                                                  .getFilePath( repository, url ) );
        final File dir = dest.getParentFile();
        if ( dir != null && !dir.exists() && !dir.mkdirs() )
        {
            throw new TransferException( "Cannot create directory: %s", dir );
        }

        return new FilePublish( dest, stream );
    }

    @Override
    public boolean handles( final Location location )
    {
        return location.getUri()
                       .startsWith( "file:" );
    }

}
