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

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenElementView
{

    //    private final Logger logger = new Logger( getClass() );

    protected final Element element;

    protected final MavenPomView pomView;

    private final String managementXpathFragment;

    private String[] managementXpaths;

    private MavenElementView managementElement;

    public MavenElementView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        this.pomView = pomView;
        this.element = element;
        this.managementXpathFragment = managementXpathFragment;
    }

    public MavenElementView( final MavenPomView pomView, final Element element )
    {
        this.pomView = pomView;
        this.element = element;
        this.managementXpathFragment = null;
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
        return pomView.containsExpression( value );
    }

    public Element getElement()
    {
        return element;
    }

    public MavenPomView getPomView()
    {
        return pomView;
    }

    public String getProfileId()
    {
        return pomView.getProfileIdFor( element );
    }

    protected String getValueWithManagement( final String named )
        throws GalleyMavenException
    {
        final String value = getValueFrom( named, element );
        //        logger.info( "Value of path: '%s' local to: %s is: '%s'\nIn: %s", named, element, value, pomView.getRef() );
        if ( value == null )
        {
            final MavenElementView mgmt = getManagementElement();
            if ( mgmt != null )
            {
                return mgmt.getValue( named );
            }
        }

        return value;
    }

    private synchronized MavenElementView getManagementElement()
        throws GalleyMavenException
    {
        if ( managementElement == null )
        {
            initManagementXpaths();
            if ( managementXpaths != null )
            {
                for ( final String xpath : managementXpaths )
                {
                    final MavenElementView e = pomView.resolveXPathToElementView( xpath, false, -1 );
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
        //        logger.info( "Resolving '%s' from node: %s", path, this.element );
        final List<Node> nodes = pomView.resolveXPathToNodeListFrom( this.element, path, true );
        if ( nodes == null || nodes.isEmpty() )
        {
            final MavenElementView managedElement = getManagementElement();
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

        final String profileId = getProfileId();
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
            //            logger.info( "Created management XPath template: '%s'", xp );
        }

        final StringBuilder sb = new StringBuilder();
        sb.append( "/project/" )
          .append( managementXpathFragment )
          .append( '[' )
          .append( qualifier )
          .append( "]" );

        final String xp = sb.toString();
        xpaths.add( xp );
        //        logger.info( "Created management XPath template: '%s'", xp );

        managementXpaths = xpaths.toArray( new String[xpaths.size()] );
    }

    protected String getValue( final String path )
    {
        return getValueFrom( path, element );
    }

    protected String getValueFrom( final String path, final Element element )
    {
        String val;
        if ( path.contains( "(\\/\\/|\\[" ) )
        {
            final Element e = (Element) pomView.resolveXPathToNodeFrom( element, path, false );

            if ( e == null )
            {
                return null;
            }

            val = e.getTextContent()
                   .trim();
        }
        else
        {
            final String[] parts = path.split( "/" );
            Element e = element;
            NodeList nl;
            for ( final String part : parts )
            {
                nl = element.getElementsByTagName( part );
                if ( nl == null || nl.getLength() < 1 )
                {
                    return null;
                }

                e = (Element) nl.item( 0 );
                if ( e == null )
                {
                    return null;
                }
            }

            val = e.getTextContent();
        }

        //        logger.info( "Resolving expressions in: '%s'", val );
        return pomView.resolveExpressions( val );
    }

    protected Node getNode( final String path )
    {
        final String[] names = path.split( "/" );
        Element e = element;
        for ( final String named : names )
        {
            if ( e == null )
            {
                break;
            }

            final NodeList matches = e.getElementsByTagName( named );
            if ( matches == null || matches.getLength() < 1 )
            {
                return null;
            }

            e = (Element) matches.item( 0 );
        }

        return e;
    }

    public String toXML()
    {
        return pomView.toXML( element );
    }

}
