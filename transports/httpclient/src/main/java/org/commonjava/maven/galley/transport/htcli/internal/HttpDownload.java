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
package org.commonjava.maven.galley.transport.htcli.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.commonjava.maven.galley.TransferContentException;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.proxy.ProxySitesCache;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.maven.galley.transport.htcli.util.HttpUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

public final class HttpDownload
    extends AbstractHttpJob
    implements DownloadJob
{

    private final Transfer target;

    private final Map<Transfer, Long> transferSizes;

    private final EventMetadata eventMetadata;

    private final ObjectMapper mapper;

    private final boolean deleteFilesOnPath;

    public HttpDownload( final String url, final HttpLocation location, final Transfer target,
                         final Map<Transfer, Long> transferSizes, final EventMetadata eventMetadata, final Http http,
                         final ObjectMapper mapper )
    {
        this( url, location, target, transferSizes, eventMetadata, http, mapper, true, null, null );
    }

    public HttpDownload( final String url, final HttpLocation location, final Transfer target,
                         final Map<Transfer, Long> transferSizes, final EventMetadata eventMetadata, final Http http,
                         final ObjectMapper mapper, final List<String> egressSites,
                         ProxySitesCache proxySitesCache )
    {
        this( url, location, target, transferSizes, eventMetadata, http, mapper, true, egressSites, proxySitesCache );
    }

    public HttpDownload( final String url, final HttpLocation location, final Transfer target,
                         final Map<Transfer, Long> transferSizes, final EventMetadata eventMetadata, final Http http,
                         final ObjectMapper mapper, final boolean deleteFilesOnPath,
                         final List<String> egressSites, ProxySitesCache proxySitesCache )
    {

        super( url, location, http, egressSites, proxySitesCache );
        this.request = new HttpGet( url );
        this.target = target;
        this.transferSizes = transferSizes;
        this.eventMetadata = eventMetadata;
        this.mapper = mapper;
        this.deleteFilesOnPath = deleteFilesOnPath;
    }

    @Override
    public DownloadJob call()
    {
        return doCall();
    }

    private DownloadJob doCall()
    {
        String oldName = Thread.currentThread().getName();
        boolean ret = false;
        try
        {
            String newName = oldName + ": GET " + url;
            Thread.currentThread().setName( newName );
            ret = executeHttp();
            if ( ret )
            {
                transferSizes.put( target, HttpUtil.getContentLength( response ) );
                writeTarget();
            }
        }
        catch ( final TransferException e )
        {
            this.error = e;
        }
        finally
        {
            cleanup();
            if ( oldName != null )
            {
                Thread.currentThread().setName( oldName );
            }
        }

        if ( ret && ( error == null ) )
        {
            logger.info( "Download attempt done: {} Result:\n  target: {}", url, target );
        }
        else
        {
            logger.info( "Download attempt failed: {} Result:\n  target: {}\n  error: {}\n  success: {}", url, target,
                         error, ret );
        }
        return this;
    }

    @Override
    protected ObjectMapper getMetadataObjectMapper()
    {
        return mapper;
    }

    @Override
    public long getTransferSize()
    {
        return response == null ? -1 : HttpUtil.getContentLength( response );
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Transfer getTransfer()
    {
        return target;
    }

    private void writeTarget()
        throws TransferException
    {
        OutputStream out = null;
        if ( response != null )
        {

            InputStream in = null;
            try
            {
                final HttpEntity entity = response.getEntity();

                in = entity.getContent();
                out = target.openOutputStream( TransferOperation.DOWNLOAD, true, eventMetadata, deleteFilesOnPath );
                doCopy( in, out );
                logger.info( "Ensuring all HTTP data is consumed..." );
            }
            catch ( final IOException eOrig )
            {
                closeAllQuietly( in, out );

                ConcreteResource resource = target.getResource();
                try
                {
                    logger.debug( "Failed to write to local proxy store:{}. Deleting partial target file:{}", eOrig,
                                  target.getPath() );
                    target.delete();
                }
                catch ( IOException eDel )
                {
                    logger.error( String.format( "Failed to delete target file: %s\nOriginal URL: %s. Reason: %s",
                                  target, url, eDel.getMessage() ), eDel );
                }

                logger.error( String.format( "Failed to write to local proxy store: %s\nOriginal URL: %s. Reason: %s", target, url,
                              eOrig.getMessage() ), eOrig );

                throw new TransferContentException( resource,
                                                    "Failed to write to local proxy store: %s\nOriginal URL: %s. Reason: %s",
                                                    eOrig, target, url, eOrig.getMessage() );
            }
            finally
            {
                closeAllQuietly( in, out );
            }
        }
    }

    /**
     * Break out {@link org.apache.commons.io.IOUtils#copy(InputStream, OutputStream)} so we can decorate it with Byteman
     * rules to test network errors.
     * @param in -
     * @param out -
     */
    private void doCopy( final InputStream in, final OutputStream out )
            throws IOException
    {
        copy( in, out );
    }

    private void closeAllQuietly( final InputStream in, final OutputStream out )
    {
        try
        {
            EntityUtils.consume( response.getEntity() );
            logger.info( "All HTTP data was consumed." );
        }
        catch ( IOException e )
        {
            logger.error( "Failed to consume remainder HTTP response entity data", e );
        }
        finally
        {
            closeQuietly( in );

            logger.info( "Closing output stream: {}", out );
            closeQuietly( out );
        }
    }

}
