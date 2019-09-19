/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.transport.htcli;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.AbstractTransferDecorator;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.commonjava.maven.galley.transport.htcli.model.HttpExchangeMetadata;
import org.commonjava.maven.galley.transport.htcli.model.HttpExchangeMetadataFromRequestHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.commonjava.maven.galley.spi.cache.CacheProvider.STORE_HTTP_HEADERS;
import static org.commonjava.maven.galley.transport.htcli.model.HttpExchangeMetadata.FILE_EXTENSION;

/**
 * This TransferDecorator is used to generate http-metadata.json when uploading artifacts. It will use {@link EventMetadata}
 * as intermediate to get the http request headers from client, and use these headers as the content for the json file. Only
 * artifacts but not metadata files will have the accompanied http-metadata file generation.
 */
@Named
@Alternative
public class UploadMetadataGenTransferDecorator
        extends AbstractTransferDecorator
{
    private static final String HTTP_METADATA_WRITE = "io.http-metadata.write";
    private static final String HTTP_METADATA_WRITE_OPEN = HTTP_METADATA_WRITE + ".open";

    private static final Logger logger = LoggerFactory.getLogger( UploadMetadataGenTransferDecorator.class );

    private SpecialPathManager specialPathManager;

    private Function<String, Timer.Context> timerProvider;

    public UploadMetadataGenTransferDecorator( SpecialPathManager specialPathManager, Function<String, Timer.Context> timerProvider )
    {
        this.specialPathManager = specialPathManager;
        this.timerProvider = timerProvider;
    }

    @Override
    public OutputStream decorateWrite( OutputStream stream, Transfer transfer, TransferOperation op,
                                       EventMetadata metadata )
            throws IOException
    {
        if ( transfer.getPath().endsWith( FILE_EXTENSION ) )
        {
            logger.debug( "NOT writing http-metadata for: {}", transfer );
            return super.decorateWrite( stream, transfer, op, metadata );
        }

        final SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer, metadata.getPackageType() );

        final Boolean isMetadata = specialPathInfo != null && specialPathInfo.isMetadata();
        final Boolean isUpload = op == TransferOperation.UPLOAD;
        final Boolean hasRequestHeaders =
                metadata.get( STORE_HTTP_HEADERS ) != null && metadata.get( STORE_HTTP_HEADERS ) instanceof Map;

        if ( isUpload && !isMetadata && hasRequestHeaders )
        {
            logger.debug( "Writing http-metadata for: {}", transfer );

            // we defer writing http-metadata.json until the main stream closes, to avoid trouble with directory-level write locks.
            return new HttpMetadataWrapperOutputStream( stream, transfer, metadata );
        }

        logger.debug( "NOT writing http-metadata for: {}", transfer );
        return super.decorateWrite( stream, transfer, op, metadata );
    }

    private void writeMetadata( final Transfer target, final ObjectMapper mapper, final Map<String, List<String>> requestHeaders )
    {
        Timer.Context writeTimer = timerProvider.apply( HTTP_METADATA_WRITE );
        logger.debug( "http-metadata write-timer is: {}", writeTimer );
        try
        {
            Transfer metaTxfr = target.getSiblingMeta( FILE_EXTENSION );
            if ( metaTxfr == null )
            {
                if ( target.isDirectory() )
                {
                    logger.trace( "DIRECTORY. Using HTTP exchange metadata file INSIDE directory called: {}", FILE_EXTENSION );
                    metaTxfr = target.getChild( FILE_EXTENSION );
                }
                else
                {
                    logger.trace( "SKIP: Cannot retrieve HTTP exchange metadata Transfer instance for: {}", target );
                    return;
                }
            }

            final HttpExchangeMetadata metadata = new HttpExchangeMetadataFromRequestHeader( requestHeaders );
            final Transfer finalMeta = metaTxfr;

            Timer.Context openTimer = timerProvider.apply( HTTP_METADATA_WRITE_OPEN );
            logger.debug( "http-metadata open-timer is: {}", openTimer );
            try(OutputStream out = metaTxfr.openOutputStream( TransferOperation.GENERATE, false ) )
            {
                if ( openTimer != null )
                {
                    openTimer.stop();
                }

                logger.trace( "Writing HTTP exchange metadata:\n\n{}\n\n", new Object()
                {
                    @Override
                    public String toString()
                    {
                        try
                        {
                            return mapper.writeValueAsString( metadata );
                        }
                        catch ( final JsonProcessingException e )
                        {
                            logger.warn( String.format("Failed to write HTTP exchange metadata: %s. Reason: %s", finalMeta, e.getMessage()), e );
                        }

                        return "ERROR RENDERING METADATA";
                    }
                } );

                mapper.writeValue( out, metadata );
            }
            catch ( final IOException e )
            {
                if ( logger.isTraceEnabled() )
                {
                    logger.trace( String.format( "Failed to write metadata for HTTP exchange to: %s. Reason: %s", metaTxfr,
                                                 e.getMessage() ), e );
                }
                else
                {
                    logger.warn( "Failed to write metadata for HTTP exchange to: {}. Reason: {}", metaTxfr, e.getMessage() );
                }
            }
        }
        finally
        {
            if ( writeTimer != null )
            {
                writeTimer.stop();
            }
        }
    }

    private class HttpMetadataWrapperOutputStream
            extends FilterOutputStream
    {
        private final Transfer transfer;

        private final EventMetadata metadata;

        public HttpMetadataWrapperOutputStream( final OutputStream delegate, final Transfer transfer,
                                                final EventMetadata metadata )
        {
            super( delegate );
            this.transfer = transfer;
            this.metadata = metadata;
        }

        @Override
        public void close()
                throws IOException
        {
            super.flush();

            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.debug( "START: Write http-metadata for: {}", transfer );

            // We only care about non-metadata artifact uploading for http-metadata generation
            @SuppressWarnings( "unchecked" ) Map<String, List<String>> storeHttpHeaders =
                    (Map<String, List<String>>) metadata.get( STORE_HTTP_HEADERS );
            writeMetadata( transfer, new ObjectMapper(), storeHttpHeaders );

            logger.debug( "END: Write http-metadata for: {}. Resuming stream close() method.", transfer );

            super.close();
        }
    }
}
