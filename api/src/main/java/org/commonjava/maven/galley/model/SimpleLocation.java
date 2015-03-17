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
package org.commonjava.maven.galley.model;

import java.util.HashMap;
import java.util.Map;

public class SimpleLocation extends Attributes
    implements Location
{

    private final boolean allowPublishing;

    private final boolean allowDownloading;

    private final boolean allowStoring;

    private final boolean allowSnapshots;

    private final boolean allowReleases;

    private final String uri;

    private final Map<String, Object> attributes = new HashMap<String, Object>();

    private final String name;

    public SimpleLocation( final String name, final String uri, final boolean allowSnapshots, final boolean allowReleases,
                           final boolean allowsStoring, final boolean allowPublishing, final boolean allowDownloading, final int timeoutSeconds )
    {
        this.name = name;
        this.uri = uri;
        this.allowSnapshots = allowSnapshots;
        this.allowReleases = allowReleases;
        this.allowStoring = allowsStoring;
        this.allowPublishing = allowPublishing;
        this.allowDownloading = allowDownloading;
        setAttribute(CONNECTION_TIMEOUT_SECONDS, timeoutSeconds);
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
            if ( other.uri != null )
            {
                return false;
            }
        }
        else if ( !uri.equals( other.uri ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public <T> T getAttribute( final String key, final Class<T> type )
    {
        final Object value = attributes.get( key );
        if ( value != null )
        {
            return type.cast( value );
        }

        return null;
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
