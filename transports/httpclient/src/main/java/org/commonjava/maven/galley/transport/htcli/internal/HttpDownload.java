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
package org.commonjava.maven.galley.transport.htcli.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.commonjava.maven.galley.TransferContentException;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.DownloadJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.maven.galley.transport.htcli.util.HttpUtil;
import org.commonjava.maven.galley.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.commonjava.maven.galley.spi.cache.CacheProvider.STORAGE_PATH;

public final class HttpDownload
    extends AbstractHttpJob
    implements DownloadJob
{

    private final Transfer target;

    private Map<Transfer, Long> transferSizes;

    private final EventMetadata eventMetadata;

    private final ObjectMapper mapper;

    private boolean deleteFilesOnPath;

    public HttpDownload( final String url, final HttpLocation location, final Transfer target,
                         final Map<Transfer, Long> transferSizes, final EventMetadata eventMetadata, final Http http,
                         final ObjectMapper mapper )
    {
        this( url, location, target, transferSizes, eventMetadata, http, mapper, true );
    }

    public HttpDownload( final String url, final HttpLocation location, final Transfer target,
                         final Map<Transfer, Long> transferSizes, final EventMetadata eventMetadata, final Http http,
                         final ObjectMapper mapper, final boolean deleteFilesOnPath )
    {
        super( url, location, http );
        this.target = target;
        this.transferSizes = transferSizes;
        this.eventMetadata = eventMetadata;
        this.mapper = mapper;
        this.deleteFilesOnPath = deleteFilesOnPath;
    }

    @Override
    public DownloadJob call() throws Exception
    {
        request = new HttpGet( url );
        try
        {
            if ( executeHttp() )
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
        }

        logger.info( "Download attempt done: {} Result:\n  target: {}\n  error: {}", url, target, error );
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
        if ( eventMetadata.get( STORAGE_PATH ) != null )
        {
            target.setResource( ResourceUtils.storageResource( target.getResource(), eventMetadata ) );
        }
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
                copy( in, out );
                logger.info( "Ensuring all HTTP data is consumed..." );
                EntityUtils.consume( entity );
                logger.info( "All HTTP data was consumed." );

            }
            catch ( final IOException e )
            {
                throw new TransferException( "Failed to write to local proxy store: {}\nOriginal URL: {}. Reason: {}",
                                             e, target, url, e.getMessage() );
            }
            finally
            {
                closeQuietly( in );

                logger.info( "Closing output stream: {}", out );
                closeQuietly( out );

                /*
                * If the server responded with 200 but the content length doesn't match,
                * we will delete the file and return a null Transfer as if the file wasn't found,
                * since the content is invalid.
                */
                Header[] headers = response.getHeaders( "Content-Length" );
                if ( headers[0] != null && !headers[0].getValue().equals( "" ) )
                {
                    long contentLength = Long.parseLong( headers[0].getValue().toString() );
                    long targetLength = target.length();
                    if ( contentLength != targetLength )
                    {
                        try
                        {
                            target.delete();
                        }
                        catch ( final Exception e )
                        {
                            throw new TransferException(
                                            "Failed to write to local proxy store: {}\nOriginal URL: {}. Reason: {}", e,
                                            target, url, e.getMessage() );
                        }
                        throw new TransferContentException( target.getResource(),
                                                            "Target: %s's Content-Length mismatch (expected: %s, got: %s)",
                                                            target.getFullPath(), contentLength, targetLength );
                    }
                }
            }
        }
    }

}
