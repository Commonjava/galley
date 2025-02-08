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

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.proxy.ProxySitesCache;
import org.commonjava.maven.galley.spi.transport.PublishJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.maven.galley.util.ContentTypeUtils;

import java.io.InputStream;

import static org.commonjava.o11yphant.trace.TraceManager.addFieldToActiveSpan;

public final class HttpPublish
    extends AbstractHttpJob
    implements PublishJob
{

    private final InputStream stream;

    private final long length;

    private final String contentType;

    private boolean success;

    public HttpPublish( final String url, final HttpLocation location, final InputStream stream, final long length,
                        final String contentType, final Http http )
    {
        super( url, location, http, null, HttpStatus.SC_OK, HttpStatus.SC_CREATED );
        this.stream = stream;
        this.length = length;
        this.contentType = contentType == null ? ContentTypeUtils.detectContent( url ) : contentType;
    }

    @Override
    public HttpPublish call()
    {
        //            logger.info( "Trying: {}", url );
        final HttpPut put = new HttpPut( url );

        addFieldToActiveSpan( "http-target", url );
        addFieldToActiveSpan( "activity", "httpclient-publish" );

        put.setEntity( new InputStreamEntity( stream, length, ContentType.create( contentType ) ) );

        request = put;

        try
        {
            success = executeHttp();
        }
        catch ( final TransferException e )
        {
            this.error = e;
        }
        finally
        {
            cleanup();
        }

        return this;
    }

    @Override
    public boolean isSuccessful()
    {
        return success;
    }

    @Override
    public long getTransferSize()
    {
        return length;
    }

}
