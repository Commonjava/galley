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
package org.commonjava.maven.galley.filearc;

import java.io.File;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.filearc.internal.FileDownload;
import org.commonjava.maven.galley.filearc.internal.FileExistence;
import org.commonjava.maven.galley.filearc.internal.FileListing;
import org.commonjava.maven.galley.filearc.internal.FilePublish;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;
import org.commonjava.maven.galley.util.PathUtils;

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
    public DownloadJob createDownloadJob( final ConcreteResource resource, final Transfer target, final int timeoutSeconds )
        throws TransferException
    {
        final File src = getFile( resource );
        return new FileDownload( target, src );
    }

    @Override
    public PublishJob createPublishJob( final ConcreteResource resource, final InputStream stream, final long length, final int timeoutSeconds )
        throws TransferException
    {
        return createPublishJob( resource, stream, length, null, timeoutSeconds );
    }

    @Override
    public PublishJob createPublishJob( final ConcreteResource resource, final InputStream stream, final long length, final String contentType,
                                        final int timeoutSeconds )
        throws TransferException
    {
        final File pubDir = config.getPubDir();
        if ( pubDir == null )
        {
            throw new TransferException( "This transport is read-only!" );
        }

        final File dest = new File( pubDir, config.getGenerator()
                                                  .getFilePath( resource ) );
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
        final String uri = location.getUri();
        return uri != null && uri.startsWith( "file:" );
    }

    @Override
    public ListingJob createListingJob( final ConcreteResource resource, final Transfer target, final int timeoutSeconds )
        throws TransferException
    {
        final File src = getFile( resource );
        return new FileListing( resource, src, target );
    }

    @Override
    public ExistenceJob createExistenceJob( final ConcreteResource resource, final int timeoutSeconds )
        throws TransferException
    {
        final File src = getFile( resource );
        return new FileExistence( src );
    }

    private File getFile( final ConcreteResource resource )
    {
        return new File( PathUtils.normalize( resource.getLocationUri(), resource.getPath() ) );
    }

}
