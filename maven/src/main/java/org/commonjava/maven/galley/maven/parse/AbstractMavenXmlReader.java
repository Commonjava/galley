/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.maven.parse;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.model.view.DocRef;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.transport.LocationExpander;

public abstract class AbstractMavenXmlReader<T extends ProjectRef>
{

    private final Map<DocCacheKey<T>, WeakReference<DocRef<T>>> cache = new ConcurrentHashMap<DocCacheKey<T>, WeakReference<DocRef<T>>>();

    @Inject
    protected XMLInfrastructure xml;

    @Inject
    protected LocationExpander locationExpander;

    protected AbstractMavenXmlReader()
    {
    }

    protected AbstractMavenXmlReader( final XMLInfrastructure xml, final LocationExpander locationExpander )
    {
        this.xml = xml;
        this.locationExpander = locationExpander;
    }

    protected synchronized void cache( final DocRef<T> dr )
    {
        cache.put( new DocCacheKey<T>( dr ), new WeakReference<DocRef<T>>( dr ) );
    }

    protected synchronized DocRef<T> getFirstCached( final T ref, final Collection<? extends Location> locations )
        throws TransferException
    {
        for ( final Location location : locationExpander.expand( locations ) )
        {
            final DocCacheKey<ProjectRef> key = new DocCacheKey<ProjectRef>( ref, location );
            final WeakReference<DocRef<T>> reference = cache.get( key );
            if ( reference != null )
            {
                final DocRef<T> dr = reference.get();
                if ( dr == null )
                {
                    cache.remove( key );
                }
                else
                {
                    return dr;
                }
            }
        }

        return null;
    }

    protected synchronized Map<Location, DocRef<T>> getAllCached( final T ref, final List<? extends Location> locations )
    {
        final Map<Location, DocRef<T>> result = new HashMap<Location, DocRef<T>>();
        for ( final Location location : locations )
        {
            final DocCacheKey<ProjectRef> key = new DocCacheKey<ProjectRef>( ref, location );
            final WeakReference<DocRef<T>> reference = cache.get( key );
            if ( reference != null )
            {
                final DocRef<T> dr = reference.get();
                if ( dr == null )
                {
                    cache.remove( key );
                }
                else
                {
                    result.put( location, dr );
                }
            }
        }

        return result;
    }

    private static final class DocCacheKey<T extends ProjectRef>
    {
        private final T ref;

        private final Object source;

        private DocCacheKey( final T ref, final Location location )
        {
            this.ref = ref;
            this.source = location;
        }

        public DocCacheKey( final DocRef<T> dr )
        {
            this.ref = dr.getRef();
            this.source = dr.getSource();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
            result = prime * result + ( ( ref == null ) ? 0 : ref.hashCode() );
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
            @SuppressWarnings( "unchecked" )
            final DocCacheKey<T> other = (DocCacheKey<T>) obj;
            if ( source == null )
            {
                if ( other.source != null )
                {
                    return false;
                }
            }
            else if ( !source.equals( other.source ) )
            {
                return false;
            }
            if ( ref == null )
            {
                if ( other.ref != null )
                {
                    return false;
                }
            }
            else if ( !ref.equals( other.ref ) )
            {
                return false;
            }
            return true;
        }
    }
}
