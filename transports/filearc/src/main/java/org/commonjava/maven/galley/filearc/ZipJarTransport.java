/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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

import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.filearc.internal.ZipDownload;
import org.commonjava.maven.galley.filearc.internal.ZipExistence;
import org.commonjava.maven.galley.filearc.internal.ZipListing;
import org.commonjava.maven.galley.filearc.internal.ZipPublish;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;
import org.commonjava.maven.galley.spi.transport.ListingJob;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.spi.transport.Transport;

@ApplicationScoped
@Named( "zip-jar-galley-transport" )
public class ZipJarTransport
    implements Transport
{

    @Override
    public DownloadJob createDownloadJob( final ConcreteResource resource, final Transfer target,
                                          final Map<Transfer, Long> transferSizes, final int timeoutSeconds, final EventMetadata eventMetadata )
    {
        return new ZipDownload( target, eventMetadata );
    }

    @Override
    public PublishJob createPublishJob( final ConcreteResource resource, final InputStream stream, final long length, final int timeoutSeconds )
    {
        return createPublishJob( resource, stream, length, null, timeoutSeconds );
    }

    @Override
    public PublishJob createPublishJob( final ConcreteResource resource, final InputStream stream, final long length, final String contentType,
                                        final int timeoutSeconds )
    {
        return new ZipPublish( resource, stream );
    }

    @Override
    public boolean handles( final Location location )
    {
        final String uri = location.getUri();
        return uri != null && ( ( uri.startsWith( "zip:" ) ) || ( uri.startsWith( "jar:" ) ) );
    }

    @Override
    public ListingJob createListingJob( final ConcreteResource resource, final Transfer target, final int timeoutSeconds )
    {
        return new ZipListing( resource, target );
    }

    @Override
    public ExistenceJob createExistenceJob( final ConcreteResource resource, final Transfer target,
                                            final int timeoutSeconds )
    {
        return new ZipExistence( resource );
    }

    @Override
    public boolean allowsCaching()
    {
        return true;
    }

}
