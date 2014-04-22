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
package org.commonjava.maven.galley.filearc;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
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
    public DownloadJob createDownloadJob( final ConcreteResource resource, final Transfer target, final int timeoutSeconds )
        throws TransferException
    {
        return new ZipDownload( target );
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
        return new ZipPublish( resource, stream );
    }

    @Override
    public boolean handles( final Location location )
    {
        final String uri = location.getUri();
        return uri != null && ( ( uri.startsWith( "zip:" ) && uri.endsWith( ".zip" ) ) || ( uri.startsWith( "jar:" ) && uri.endsWith( ".jar" ) ) );
    }

    @Override
    public ListingJob createListingJob( final ConcreteResource resource, final int timeoutSeconds )
        throws TransferException
    {
        return new ZipListing( resource );
    }

    @Override
    public ExistenceJob createExistenceJob( final ConcreteResource resource, final int timeoutSeconds )
        throws TransferException
    {
        return new ZipExistence( resource );
    }

}
