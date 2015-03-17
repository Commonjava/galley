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

import static org.commonjava.maven.galley.util.PathUtils.ROOT;
import static org.commonjava.maven.galley.util.PathUtils.normalize;
import static org.commonjava.maven.galley.util.PathUtils.parentPath;

import java.util.Map;

public class ConcreteResource extends Attributes
    implements Resource
{

    final Location location;

    final String path;

    public ConcreteResource( final Location location, final String... path )
    {
        this.path = normalize( path );
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
        if ( !( obj instanceof ConcreteResource ) )
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

    @Override
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

    public boolean isRoot()
    {
        return path == ROOT || ROOT.equals( path );
    }

    public Resource getParent()
    {
        if ( isRoot() )
        {
            return null;
        }

        return new ConcreteResource( location, parentPath( path ) );
    }

    public Resource getChild( final String file )
    {
        return new ConcreteResource( location, normalize( path, file ) );
    }

    public String getPath()
    {
        return path;
    }
}
