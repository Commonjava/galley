/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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

import static org.commonjava.maven.galley.util.PathUtils.ROOT;
import static org.commonjava.maven.galley.util.PathUtils.normalize;
import static org.commonjava.maven.galley.util.PathUtils.parentPath;

public class ConcreteResource
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

    /* (non-Javadoc)
     * @see org.commonjava.maven.galley.model.Resource#allowsDeletion()
     */
    @Override
    public boolean allowsDeletion()
    {
        return location.allowsDeletion();
    }

    public String getLocationUri()
    {
        return location.getUri();
    }

    public String getLocationName()
    {
        return location.getName();
    }

    public boolean isRoot()
    {
        return path == ROOT || ROOT.equals( path );
    }

    public ConcreteResource getParent()
    {
        if ( isRoot() )
        {
            return null;
        }

        return new ConcreteResource( location, parentPath( path ) );
    }

    public ConcreteResource getChild( final String file )
    {
        return new ConcreteResource( location, normalize( path, file ) );
    }

    public String getPath()
    {
        return path;
    }
}
