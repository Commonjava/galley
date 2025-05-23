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
import org.apache.http.client.methods.HttpHead;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.proxy.ProxySitesCache;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

import java.util.List;

import static org.commonjava.o11yphant.trace.TraceManager.addFieldToActiveSpan;

public final class HttpExistence
        extends AbstractHttpJob
        implements ExistenceJob
{

    private final ObjectMapper mapper;

    private final Transfer transfer;

    public HttpExistence( final String url, final HttpLocation location, final Transfer transfer, final Http http,
                          final ObjectMapper mapper )
    {
        this( url, location, transfer, http, mapper, null, null );
    }

    public HttpExistence( final String url, final HttpLocation location, final Transfer transfer, final Http http,
                          final ObjectMapper mapper, final List<String> egressSites, ProxySitesCache proxySitesCache )
    {
        super( url, location, http, egressSites, proxySitesCache );
        this.transfer = transfer;
        this.mapper = mapper;
    }

    @Override
    public Boolean call()
    {
        String oldName = Thread.currentThread().getName();

        request = new HttpHead( url );

        addFieldToActiveSpan( "http-target", url );
        addFieldToActiveSpan( "activity", "httpclient-existence" );

        try
        {
            String newName = oldName + ": EXISTS " + url;
            Thread.currentThread().setName( newName );

            if ( executeHttp() )
            {
                return true;
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

        return false;
    }

    @Override
    protected Transfer getTransfer()
    {
        return transfer;
    }

    @Override
    protected ObjectMapper getMetadataObjectMapper()
    {
        return mapper;
    }
}
