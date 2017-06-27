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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class VirtualResource
    implements Resource, Iterable<ConcreteResource>
{

    private final List<ConcreteResource> resources;

    public VirtualResource( final List<? extends Location> locations, final String... path )
    {
        final List<ConcreteResource> resources = new ArrayList<ConcreteResource>();
        for ( final Location location : locations )
        {
            resources.add( new ConcreteResource( location, path ) );
        }
        this.resources = resources;
    }

    public VirtualResource( final List<ConcreteResource> resources )
    {
        this.resources = resources;
    }

    public VirtualResource( final ConcreteResource... resources )
    {
        this.resources = Arrays.asList( resources );
    }

    @Override
    public boolean allowsDownloading()
    {
        for ( final ConcreteResource resource : resources )
        {
            if ( resource.allowsDownloading() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsPublishing()
    {
        for ( final ConcreteResource resource : resources )
        {
            if ( resource.allowsPublishing() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsStoring()
    {
        for ( final ConcreteResource resource : resources )
        {
            if ( resource.allowsStoring() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsSnapshots()
    {
        for ( final ConcreteResource resource : resources )
        {
            if ( resource.allowsSnapshots() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsReleases()
    {
        for ( final ConcreteResource resource : resources )
        {
            if ( resource.allowsReleases() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsDeletion()
    {
        for ( final ConcreteResource resource : resources )
        {
            if ( resource.allowsDeletion() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public Iterator<ConcreteResource> iterator()
    {
        return resources.iterator();
    }

    public List<ConcreteResource> toConcreteResources()
    {
        return resources;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( resources == null ) ? 0 : resources.hashCode() );
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
        if ( !( obj instanceof VirtualResource ) )
        {
            return false;
        }
        final VirtualResource other = (VirtualResource) obj;
        if ( resources == null )
        {
            if ( other.resources != null )
            {
                return false;
            }
        }
        else if ( !resources.equals( other.resources ) )
        {
            return false;
        }
        return true;
    }

}
