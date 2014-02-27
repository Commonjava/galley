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
package org.commonjava.maven.galley.maven.model.view;

import static org.commonjava.maven.galley.maven.model.view.XPathManager.A;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.G;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;

public class MavenGAView
    extends MavenElementView
    implements ProjectRefView
{

    private String groupId;

    private String artifactId;

    public MavenGAView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element, managementXpathFragment );
    }

    public MavenGAView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element, null );
    }

    @Override
    public synchronized String getGroupId()
    {
        if ( groupId == null )
        {
            groupId = getValue( G );
        }

        return groupId;
    }

    protected void setGroupId( final String groupId )
    {
        this.groupId = groupId;
    }

    @Override
    public synchronized String getArtifactId()
    {
        if ( artifactId == null )
        {
            artifactId = getValue( A );
        }

        return artifactId;
    }

    @Override
    public ProjectRef asProjectRef()
        throws GalleyMavenException
    {
        try
        {
            return new ProjectRef( getGroupId(), getArtifactId() );
        }
        catch ( final IllegalArgumentException e )
        {
            throw new GalleyMavenException( "Cannot render ProjectRef: {}:{}. Reason: {}", e, getGroupId(), getArtifactId(), e.getMessage() );
        }
    }

    @Override
    public String toString()
    {
        return String.format( "%s [%s:%s]", getClass().getSimpleName(), getGroupId(), getArtifactId() );
    }

    public boolean isValid()
    {
        return !containsExpression( getGroupId() ) && !containsExpression( getArtifactId() );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        final String artifactId = getArtifactId();
        final String groupId = getGroupId();

        result = prime * result + ( ( artifactId == null ) ? 0 : artifactId.hashCode() );
        result = prime * result + ( ( groupId == null ) ? 0 : groupId.hashCode() );
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
        final String artifactId = getArtifactId();
        final String groupId = getGroupId();

        final MavenGAView other = (MavenGAView) obj;
        final String oArtifactId = other.getArtifactId();
        final String oGroupId = other.getGroupId();

        if ( artifactId == null )
        {
            if ( oArtifactId != null )
            {
                return false;
            }
        }
        else if ( !artifactId.equals( oArtifactId ) )
        {
            return false;
        }
        if ( groupId == null )
        {
            if ( oGroupId != null )
            {
                return false;
            }
        }
        else if ( !groupId.equals( oGroupId ) )
        {
            return false;
        }
        return true;
    }

}
