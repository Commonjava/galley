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
package org.commonjava.maven.galley.model;

import java.util.Map;

public class ConcreteResource
    extends AbstractResource
    implements Resource
{

    final Location location;

    public ConcreteResource( final Location location, final String... path )
    {
        super( path );
        this.location = location;
    }

    public Location getLocation()
    {
        return location;
    }

    @Override
    public String toString()
    {
        return String.format( "Resource [location=%s, path=%s]", location, getPath() );
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.model.Resource#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( location == null ) ? 0 : location.hashCode() );
        result = prime * result + ( ( getPath() == null ) ? 0 : getPath().hashCode() );
        return result;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.model.Resource#equals(java.lang.Object)
     */
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
        final ConcreteResource other = (ConcreteResource) obj;
        if ( location == null )
        {
            if ( other.location != null )
            {
                return false;
            }
        }
        else if ( !location.equals( other.location ) )
        {
            return false;
        }
        final String path = getPath();
        final String otherPath = other.getPath();
        if ( path == null )
        {
            if ( otherPath != null )
            {
                return false;
            }
        }
        else if ( !path.equals( otherPath ) )
        {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.model.Resource#allowsDownloading()
     */
    @Override
    public boolean allowsDownloading()
    {
        return location.allowsDownloading();
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.model.Resource#allowsPublishing()
     */
    @Override
    public boolean allowsPublishing()
    {
        return location.allowsPublishing();
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.model.Resource#allowsStoring()
     */
    @Override
    public boolean allowsStoring()
    {
        return location.allowsStoring();
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.model.Resource#allowsSnapshots()
     */
    @Override
    public boolean allowsSnapshots()
    {
        return location.allowsSnapshots();
    }

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.model.Resource#allowsReleases()
     */
    @Override
    public boolean allowsReleases()
    {
        return location.allowsReleases();
    }

    public String getLocationUri()
    {
        return location.getUri();
    }

    public String getLocationName()
    {
        return location.getName();
    }

    public int getTimeoutSeconds()
    {
        return location.getTimeoutSeconds();
    }

    public Map<String, Object> getAttributes()
    {
        return location.getAttributes();
    }

    public <T> T getAttribute( final String key, final Class<T> type )
    {
        return location.getAttribute( key, type );
    }

    public Object removeAttribute( final String key )
    {
        return location.removeAttribute( key );
    }

    public Object setAttribute( final String key, final Object value )
    {
        return location.setAttribute( key, value );
    }

    @Override
    protected Resource newDerivedResource( final String... path )
    {
        return new ConcreteResource( location, path );
    }
}
