/**
 * Copyright (C) 2021 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.transport.htcli.model;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Purpose of this class is to change the behaviour of the name field in specific
 * situation(s). Primarily in this case to provide a managed repository co-ordinate
 * for caching http connection pools.
 * Based on commonjava/indy:indy-model-core-java StoreKey.
 *
 */
public class ManagedRepositoryLocationDecorator
    implements HttpLocation 
{

    private HttpLocation location;

    public ManagedRepositoryLocationDecorator ( HttpLocation loc )
    {
        this.location = loc;
    }

    @Override
    public boolean allowsDownloading()
    {
        return location.allowsDownloading();
    }

    @Override
    public boolean allowsPublishing()
    {
        return location.allowsPublishing();
    }

    @Override
    public boolean allowsStoring()
    {
        return location.allowsStoring();
    }

    @Override
    public boolean allowsSnapshots()
    {
        return location.allowsSnapshots();
    }

    @Override
    public boolean allowsReleases()
    {
        return location.allowsReleases();
    }

    @Override
    public boolean allowsDeletion()
    {
        return false;
    }

    @Override
    public String getUri()
    {
        return location.getUri();
    }

    @Override
    public String getName()
    {
        String[] parts = location.getName().split( ":" );
        if ( parts.length > 1 )
        {
            return String.format( "%1$s:%2$s", parts[0], parts[1] );
        }
        else if ( parts.length > 0 )
        {
            return String.format( "%1$s", parts[0] );
        }
        return location.getName();
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        return location.getAttributes();
    }

    @Override
    public <T> T getAttribute(String key, Class<T> type) 
    {
        return location.getAttribute(key, type);
    }

    @Override
    public <T> T getAttribute(String key, Class<T> type, T defaultValue) 
    {
        return location.getAttribute( key, type, defaultValue );
    }

    @Override
    public Object removeAttribute(String key) 
    {
        return location.removeAttribute( key );
    }

    @Override
    public Object setAttribute(String key, Object value) 
    {
        return location.setAttribute( key, value );
    }

    @Override
    public String getKeyCertPem()
    {
        return location.getKeyCertPem();
    }

    @Override
    public String getServerCertPem()
    {
        return location.getServerCertPem();
    }

    @Override
    public LocationTrustType getTrustType()
    {
        return location.getTrustType();
    }

    @Override
    public String getHost()
    {
        return location.getHost();
    }

    @Override
    public int getPort()
    {
        return location.getPort();
    }

    @Override
    public String getUser()
    {
        return location.getUser();
    }

    @Override
    public String getProxyHost()
    {
        return location.getProxyHost();
    }

    @Override
    public String getProxyUser()
    {
        return location.getProxyUser();
    }

    @Override
    public int getProxyPort()
    {
        return location.getProxyPort();
    }

    @Override
    public boolean isIgnoreHostnameVerification()
    {
        return location.isIgnoreHostnameVerification();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( obj == this )
        {
            return true;
        }
        if ( obj.getClass() != getClass() )
        {
            return false;
        }
        ManagedRepositoryLocationDecorator that = (ManagedRepositoryLocationDecorator) obj;
        return new EqualsBuilder()
                .append( getName(), that.getName() )
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append( this.getName() )
                .toHashCode();
    }
}
