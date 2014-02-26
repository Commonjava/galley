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

import static org.commonjava.maven.galley.maven.model.view.XPathManager.V;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class MavenGAVView
    extends MavenGAView
    implements ProjectVersionRefView
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private String version;

    public MavenGAVView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element, managementXpathFragment );
    }

    public MavenGAVView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element );
    }

    @Override
    public synchronized String getVersion()
        throws GalleyMavenException
    {
        if ( version == null )
        {
            //            final Logger logger = LoggerFactory.getLogger( getClass() );
            //            logger.info( "Resolving version for: {}[{}:{}]\nIn: {}", getClass().getSimpleName(), getGroupId(), getArtifactId(), pomView.getRef() );
            version = getValueWithManagement( V );
        }

        return version;
    }

    @Override
    public ProjectVersionRef asProjectVersionRef()
        throws GalleyMavenException
    {
        try
        {
            return new ProjectVersionRef( getGroupId(), getArtifactId(), getVersion() );
        }
        catch ( final IllegalArgumentException e )
        {
            throw new GalleyMavenException( "Cannot render ProjectVersionRef: {}:{}:{}. Reason: {}", e, getGroupId(), getArtifactId(), getVersion(),
                                            e.getMessage() );
        }
    }

    protected void setVersion( final String version )
    {
        this.version = version;
    }

    @Override
    public String toString()
    {
        return String.format( "{} [{}:{}:{}]", getClass().getSimpleName(), getGroupId(), getArtifactId(), version == null ? "unresolved" : version );
    }

    @Override
    public boolean isValid()
    {
        try
        {
            return super.isValid() && !containsExpression( getVersion() );
        }
        catch ( final GalleyMavenException e )
        {
            logger.warn( "Failed to lookup management element. Reason: {}", e, e.getMessage() );
        }

        return false;
    }

}
