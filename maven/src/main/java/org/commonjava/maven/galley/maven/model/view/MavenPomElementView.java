/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenPomElementView
    extends AbstractMavenElementView<MavenPomView>
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final String managementXpathFragment;

    private String[] managementXpaths;

    private MavenPomElementView managementElement;

    public MavenPomElementView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element );
        this.managementXpathFragment = managementXpathFragment;
    }

    public MavenPomElementView( final MavenPomView pomView, final Element element )
    {
        this( pomView, element, null );
    }

    /**
     * Override this to provide the xpath fragment used to find management values for this view.
     */
    protected String getManagedViewQualifierFragment()
    {
        return null;
    }

    protected boolean containsExpression( final String value )
    {
        return xmlView.containsExpression( value );
    }

    public MavenPomView getPomView()
    {
        return xmlView;
    }

    public String getProfileId()
    {
        return xmlView.getProfileIdFor( element );
    }

    protected String getValueWithManagement( final String named )
        throws GalleyMavenException
    {
        final String value = getValue( named );
        //        logger.info( "Value of path: '{}' local to: {} is: '{}'\nIn: {}", named, element, value, pomView.getRef() );
        if ( value == null )
        {
            final MavenPomElementView mgmt = getManagementElement();
            if ( mgmt != null )
            {
                return mgmt.getValue( named );
            }
        }

        return value;
    }

    private synchronized MavenPomElementView getManagementElement()
        throws GalleyMavenException
    {
        if ( managementElement == null )
        {
            initManagementXpaths();
            if ( managementXpaths != null )
            {
                for ( final String xpath : managementXpaths )
                {
                    final MavenPomElementView e = xmlView.resolveXPathToElementView( xpath, false, -1 );
                    if ( e != null )
                    {
                        managementElement = e;
                        break;
                    }
                }
            }
        }

        return managementElement;
    }

    protected List<Node> getFirstNodesWithManagement( final String path )
        throws GalleyMavenException
    {
        //        logger.info( "Resolving '{}' from node: {}", path, this.element );
        final List<Node> nodes = xmlView.resolveXPathToNodeListFrom( elementContext, path, true );
        if ( nodes == null || nodes.isEmpty() )
        {
            final MavenPomElementView managedElement = getManagementElement();
            if ( managedElement != null )
            {
                return managedElement.getFirstNodesWithManagement( path );
            }
        }

        return nodes;
    }

    private void initManagementXpaths()
    {
        if ( managementXpathFragment == null )
        {
            return;
        }

        final String qualifier = getManagedViewQualifierFragment();
        if ( qualifier == null )
        {
            return;
        }

        final List<String> xpaths = new ArrayList<String>();

        final Set<String> activeProfiles = new HashSet<String>( xmlView.getActiveProfileIds() );
        activeProfiles.add( getProfileId() );

        for ( final String profileId : activeProfiles )
        {
            if ( profileId != null )
            {
                final StringBuilder sb = new StringBuilder();

                sb.append( "/project/profiles/profile[id/text()=\"" )
                  .append( profileId )
                  .append( "\"]/" )
                  .append( managementXpathFragment )
                  .append( '[' )
                  .append( qualifier )
                  .append( "]" );

                final String xp = sb.toString();
                xpaths.add( xp );
                //            logger.info( "Created management XPath template: '{}'", xp );
            }
        }

        final StringBuilder sb = new StringBuilder();
        sb.append( "/project/" )
          .append( managementXpathFragment )
          .append( '[' )
          .append( qualifier )
          .append( "]" );

        final String xp = sb.toString();
        xpaths.add( xp );
        //        logger.info( "Created management XPath template: '{}'", xp );

        managementXpaths = xpaths.toArray( new String[xpaths.size()] );
    }

    @Override
    protected String getValue( final String path )
    {
        final String val = super.getValue( path );
        if ( getProfileId() == null )
        {
            return xmlView.resolveExpressions( val );
        }
        else
        {
            return xmlView.resolveExpressions( val, getProfileId() );
        }
    }

}
