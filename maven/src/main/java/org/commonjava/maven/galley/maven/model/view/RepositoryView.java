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
package org.commonjava.maven.galley.maven.model.view;

import org.w3c.dom.Element;

public class RepositoryView
    extends AbstractMavenElementView<MavenPomView>
{

    private String url;

    private String name;

    private String id;

    public RepositoryView( final MavenPomView xmlView, final Element element )
    {
        super( xmlView, element );
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

}
