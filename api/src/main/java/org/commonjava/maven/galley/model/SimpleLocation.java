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
package org.commonjava.maven.galley.model;

import java.util.HashMap;
import java.util.Map;

public class SimpleLocation
    implements Location
{
    private final boolean allowPublishing;

    private final boolean allowDownloading;

    private final boolean allowStoring;

    private final boolean allowSnapshots;

    private final boolean allowReleases;

    private final boolean allowDeletion;

    private final String uri;

    private final Map<String, Object> attributes = new HashMap<>();

    private final String name;

    public SimpleLocation( final String name, final String uri, final boolean allowSnapshots, final boolean allowReleases,
                           final boolean allowsStoring, final boolean allowPublishing, final boolean allowDownloading,
                           final boolean allowDeletion )
    {
        this.name = name;
        this.uri = uri;
        this.allowSnapshots = allowSnapshots;
        this.allowReleases = allowReleases;
        this.allowStoring = allowsStoring;
        this.allowPublishing = allowPublishing;
        this.allowDownloading = allowDownloading;
        this.allowDeletion = allowDeletion;
    }

    public SimpleLocation( final String name, final String uri, final boolean allowSnapshots, final boolean allowReleases,
                           final boolean allowsStoring, final boolean allowPublishing, final boolean allowDownloading)
    {
        this.name = name;
        this.uri = uri;
        this.allowSnapshots = allowSnapshots;
        this.allowReleases = allowReleases;
        this.allowStoring = allowsStoring;
        this.allowPublishing = allowPublishing;
        this.allowDownloading = allowDownloading;
        this.allowDeletion = true;
    }

    public SimpleLocation( final String name, final String uri )
    {
        this.uri = uri;
        this.name = name;
        this.allowReleases = true;
        this.allowSnapshots = false;
        this.allowStoring = true;
        this.allowDownloading = true;
        this.allowPublishing = false;
        this.allowDeletion = true;
    }

    public SimpleLocation( final String uri )
    {
        this.uri = uri;
        this.name = uri;
        this.allowReleases = true;
        this.allowSnapshots = false;
        this.allowStoring = true;
        this.allowDownloading = true;
        this.allowPublishing = false;
        this.allowDeletion = true;
    }

    @Override
    public boolean allowsStoring()
    {
        return allowStoring;
    }

    @Override
    public boolean allowsDownloading()
    {
        return allowDownloading;
    }

    @Override
    public boolean allowsPublishing()
    {
        return allowPublishing;
    }

    @Override
    public boolean allowsSnapshots()
    {
        return allowSnapshots;
    }

    @Override
    public boolean allowsReleases()
    {
        return allowReleases;
    }

    @Override
    public boolean allowsDeletion()
    {
        return allowDeletion;
    }

    @Override
    public String getUri()
    {
        return uri;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( uri == null ) ? 0 : uri.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final SimpleLocation other = (SimpleLocation) obj;
        if ( uri == null )
        {
            return other.uri == null;
        }
        else
        {
            return uri.equals( other.uri );
        }
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    @Override
    public <T> T getAttribute( final String key, final Class<T> type )
    {
        return getAttribute( key, type, null );
    }

    @Override
    public <T> T getAttribute( final String key, final Class<T> type, final T defaultValue )
    {
        final Object value = attributes.get( key );
        if ( value != null )
        {
            return type.cast( value );
        }
        else
        {
            return defaultValue;
        }
    }

    @Override
    public Object removeAttribute( final String key )
    {
        return attributes.remove( key );
    }

    @Override
    public Object setAttribute( final String key, final Object value )
    {
        return attributes.put( key, value );
    }

    @Override
    public String toString()
    {
        return String.format( "SimpleLocation [uri=%s]", uri );
    }

}
