package org.commonjava.maven.galley.filearc;

import java.io.File;
import java.io.InputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.filearc.internal.FileDownload;
import org.commonjava.maven.galley.filearc.internal.FilePublish;
import org.commonjava.maven.galley.io.PathGenerator;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;

public class FileTransport
    implements Transport
{

    private final PathGenerator generator;

    private final File srcDir;

    public FileTransport( final File srcDir, final PathGenerator generator )
    {
        this.srcDir = srcDir;
        this.generator = generator;

    }

    @Override
    public DownloadJob createDownloadJob( final String url, final Location repository, final Transfer target,
                                          final int timeoutSeconds )
        throws TransferException
    {
        final File src = new File( srcDir, generator.getFilePath( repository, url ) );
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
        final File dest = new File( srcDir, generator.getFilePath( repository, url ) );
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
