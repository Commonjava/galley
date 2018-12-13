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
package org.commonjava.maven.galley.maven.model.view;

import org.w3c.dom.Element;

public class RepositoryView
    extends MavenPomElementView
{

    private String url;

    private String name;

    private String id;

    public RepositoryView( final MavenPomView xmlView, final Element element, OriginInfo originInfo )
    {
        super( xmlView, element, originInfo );
    }

    public synchronized String getUrl()
    {
        if ( url == null )
        {
            url = getValue( "url/text()" );
        }

        return url;
    }

    public synchronized String getName()
    {
        if ( name == null )
        {
            name = getValue( "name/text()" );
        }

        if ( name == null )
        {
            name = getId();
        }

        return name;
    }

    public String getId()
    {
        if ( id == null )
        {
            id = getValue( "id/text()" );
        }

        return id;
    }

    @Override
    public String toString()
    {
        return String.format( "RepositoryView [%s] (url=%s, id=%s)", getName(), getUrl(), getId() );
    }

    public boolean isReleasesEnabled()
    {
        final String value = getValue( "releases/enabled/text()" );
        return value == null || Boolean.parseBoolean( value );
    }

    public boolean isSnapshotsEnabled()
    {
        final String value = getValue( "snapshots/enabled/text()" );
        return value != null && Boolean.parseBoolean( value );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        final String repositoryId = getId();
        result = prime * result + ( ( repositoryId == null ) ? 0 : repositoryId.hashCode() );
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
        final String repositoryId = getId();
        final RepositoryView other = (RepositoryView) obj;
        final String oRepositoryId = other.getId();

        if ( repositoryId == null )
        {
            return oRepositoryId == null;
        }
        else
        {
            return repositoryId.equals( oRepositoryId );
        }

    }
}
