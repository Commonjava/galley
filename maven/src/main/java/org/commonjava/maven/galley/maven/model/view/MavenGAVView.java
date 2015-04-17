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
        return String.format( "%s [%s:%s:%s]", getClass().getSimpleName(), getGroupId(), getArtifactId(), version == null ? "unresolved" : version );
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
