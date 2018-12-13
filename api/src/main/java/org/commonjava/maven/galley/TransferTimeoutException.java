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
package org.commonjava.maven.galley;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.util.UrlUtils;

import java.net.MalformedURLException;

public class TransferTimeoutException
    extends TransferLocationException
{

    private static final long serialVersionUID = 1L;

    private final String url;


    public TransferTimeoutException( final Location location, final String url, final String format, final Object... params )
    {
        super( location, format, params );
        this.url = url;
    }

    public TransferTimeoutException( final Location location, final String url, final String format, final Throwable error, final Object... params )
    {
        super( location, format, error, params );
        this.url = url;
    }

    public TransferTimeoutException( final Transfer target, final String format, final Object... params )
    {
        super( target.getLocation(), format, params );
        this.url = composeUrl( target.getResource() );
    }

    public TransferTimeoutException( final Transfer target, final String format, final Throwable error, final Object... params )
    {
        super( target.getLocation(), format, error, params );
        this.url = composeUrl( target.getResource() );
    }

    public TransferTimeoutException( final ConcreteResource resource, final String format, final Object... params )
    {
        super( resource.getLocation(), format, params );
        this.url = composeUrl( resource );
    }


    @Override
    public String getMessage()
    {
        return super.getMessage() + "\nTimed out URL: " + getUrl();
    }

    public String getUrl()
    {
        return url;
    }


    private String composeUrl( final ConcreteResource resource )
    {
        String u;
        try
        {
            u = UrlUtils.buildUrl( resource );
        }
        catch ( MalformedURLException e )
        {
            u = resource.getLocation().getUri() + resource.getPath();
        }
        return u;
    }

}
