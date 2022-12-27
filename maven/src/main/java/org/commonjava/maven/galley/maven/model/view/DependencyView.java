/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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

import org.commonjava.atlas.maven.ident.DependencyScope;
import org.commonjava.atlas.maven.ident.ref.ArtifactRef;
import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.commonjava.atlas.maven.ident.ref.SimpleVersionlessArtifactRef;
import org.commonjava.atlas.maven.ident.ref.VersionlessArtifactRef;
import org.commonjava.atlas.maven.ident.version.InvalidVersionSpecificationException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.commonjava.maven.galley.maven.model.view.XPathManager.A;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.AND;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.C;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.END_PAREN;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.EQQUOTE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.G;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.NOT;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.OPEN_PAREN;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.OR;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.QUOTE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.RESOLVE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.T;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.TEXT;

public class DependencyView
    extends MavenGAVView
{

    private static final String S = "scope";

    private static final String OPTIONAL = "optional";

    private static final String EXCLUSIONS = "exclusions/exclusion";

    private String classifier;

    private String type;

    private DependencyScope scope;

    private Boolean optional;

    private Set<ProjectRefView> exclusions;

    public DependencyView( final MavenPomView pomView, final Element element, final OriginInfo originInfo )
    {
        super( pomView, element, originInfo, "dependencyManagement/dependencies/dependency" );
    }

    public boolean isManaged()
    {
        return xmlView.resolveXPathToNodeFrom( elementContext, "ancestor::dependencyManagement", true ) != null;
    }

    public synchronized String getClassifier()
    {
        if ( classifier == null )
        {
            classifier = getValue( C );
        }

        return classifier;
    }

    public synchronized String getRawType()
    {
        if ( type == null )
        {
            type = getValue( T );
        }

        return type;
    }

    public synchronized String getType()
    {
        final String type = getRawType();
        return type == null ? "jar" : type;
    }

    public synchronized DependencyScope getScope()
        throws GalleyMavenException
    {
        if ( scope == null )
        {
            final String s = getValueWithManagement( S );
            scope = DependencyScope.getScope( s );
        }

        return scope == null ? DependencyScope.compile : scope;
    }

    public synchronized boolean isOptional()
    {
        if ( optional == null )
        {
            final String val = getValue( OPTIONAL );
            optional = Boolean.parseBoolean( val );
        }

        return optional;
    }

    public synchronized Set<ProjectRefView> getExclusions()
        throws GalleyMavenException
    {
        if ( exclusions == null )
        {
            final List<XmlNodeInfo> nodes = getFirstNodesWithManagement( EXCLUSIONS );
            if ( nodes != null )
            {
                final Set<ProjectRefView> exclusions = new HashSet<>();
                for ( final XmlNodeInfo node : nodes )
                {
                    exclusions.add( new MavenGAView( xmlView, (Element) node.getNode(), node.getOriginInfo() ) );
                }

                this.exclusions = exclusions;
            }
        }

        return exclusions;
    }

    @Override
    protected String getManagedViewQualifierFragment()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append( RESOLVE )
          .append( G )
          .append( TEXT )
          .append( END_PAREN )
          .append( EQQUOTE )
          .append( getGroupId() )
          .append( QUOTE )
          .append( AND )
          .append( RESOLVE )
          .append( A )
          .append( TEXT )
          .append( END_PAREN )
          .append( EQQUOTE )
          .append( getArtifactId() )
          .append( QUOTE );

        final String cls = getClassifier();
        if ( cls != null )
        {
            sb.append( AND )
              .append( RESOLVE )
              .append( C )
              .append( TEXT )
              .append( END_PAREN )
              .append( EQQUOTE )
              .append( cls )
              .append( QUOTE );
        }
        else
        {
            sb.append( AND )
              .append( NOT )
              .append( C )
              .append( END_PAREN );
        }

        final String type = getRawType();
        if ( type != null )
        {
            sb.append( AND )
              .append( RESOLVE )
              .append( T )
              .append( TEXT )
              .append( END_PAREN )
              .append( EQQUOTE )
              .append( type )
              .append( QUOTE );
        }
        else
        {
            sb.append( AND )
              .append( OPEN_PAREN )
              .append( NOT )
              .append( T )
              .append( END_PAREN )
              .append( OR )
              .append( RESOLVE )
              .append( T )
              .append( TEXT )
              .append( END_PAREN )
              .append( EQQUOTE )
              .append( "jar" )
              .append( QUOTE )
              .append( END_PAREN );
        }

        return sb.toString();
    }

    public ArtifactRef asArtifactRef()
        throws GalleyMavenException
    {
        try
        {
            return new SimpleArtifactRef( asProjectVersionRef(), getType(), getClassifier() );
        }
        catch ( final IllegalArgumentException | InvalidVersionSpecificationException e )
        {
            final String classifier = getClassifier();
            throw new GalleyMavenException( "Cannot render SimpleArtifactRef: {}:{}:{}:{}{}. Reason: {}", e, getGroupId(), getArtifactId(), getVersion(),
                                            getRawType(), ( classifier == null ? "" : ":" + classifier ), e.getMessage() );
        }
    }

    public VersionlessArtifactRef asVersionlessArtifactRef()
        throws GalleyMavenException
    {
        try
        {
            return new SimpleVersionlessArtifactRef( asProjectRef(), getType(), getClassifier() );
        }
        catch ( final IllegalArgumentException | InvalidVersionSpecificationException e )
        {
            final String classifier = getClassifier();
            throw new GalleyMavenException( "Cannot render VersionlessArtifactRef: {}:{}:{}{}. Reason: {}", e, getGroupId(), getArtifactId(),
                                            getRawType(), ( classifier == null ? "" : ":" + classifier ), e.getMessage() );
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        final String artifactId = getArtifactId();
        final String groupId = getGroupId();
        final String type = getType();
        final String classifier = getClassifier();

        result = prime * result + ( ( artifactId == null ) ? 0 : artifactId.hashCode() );
        result = prime * result + ( ( groupId == null ) ? 0 : groupId.hashCode() );
        result = prime * result + ( ( type.equals( "jar" ) ) ? 0 : type.hashCode() );
        result = prime * result + ( ( classifier == null ) ? 0 : classifier.hashCode() );
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
        final String type = getType();
        final String classifier = getClassifier();

        final DependencyView other = (DependencyView) obj;
        final String oArtifactId = other.getArtifactId();
        final String oGroupId = other.getGroupId();
        final String otype = other.getType();
        final String oclassifier = other.getClassifier();

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

        if ( classifier == null )
        {
            if ( oclassifier != null )
            {
                return false;
            }
        }
        else if ( !classifier.equals( oclassifier ) )
        {
            return false;
        }

        return type.equals( otype );
    }
}
