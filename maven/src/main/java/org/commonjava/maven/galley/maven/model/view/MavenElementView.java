/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.parse.JXPathUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenElementView
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    protected final Element element;

    protected final JXPathContext elementContext;

    protected final MavenPomView pomView;

    private final String managementXpathFragment;

    private String[] managementXpaths;

    private MavenElementView managementElement;

    public MavenElementView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        this.pomView = pomView;
        this.element = element;
        this.managementXpathFragment = managementXpathFragment;

        this.elementContext = JXPathUtils.newContext( element );
    }

    public MavenElementView( final MavenPomView pomView, final Element element )
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
        final String value = getValue( named );
        //        logger.info( "Value of path: '{}' local to: {} is: '{}'\nIn: {}", named, element, value, pomView.getRef() );
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
        //        logger.info( "Resolving '{}' from node: {}", path, this.element );
        final List<Node> nodes = pomView.resolveXPathToNodeListFrom( elementContext, path, true );
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

        final Set<String> activeProfiles = new HashSet<String>( pomView.getActiveProfileIds() );
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

    protected String getValue( final String path )
    {
        String val;
        //                if ( path.contains( "(\\/\\/|\\[" ) )
        //                {
        final Element e = (Element) pomView.resolveXPathToNodeFrom( elementContext, path, false );

        if ( e == null )
        {
            return null;
        }

        val = e.getTextContent()
               .trim();
        //                }
        //        else
        //        {
        //            final String[] parts = path.split( "/" );
        //            Element e = element;
        //            NodeList nl;
        //            for ( final String part : parts )
        //            {
        //                nl = element.getElementsByTagName( part );
        //                if ( nl == null || nl.getLength() < 1 )
        //                {
        //                    return null;
        //                }
        //
        //                e = (Element) nl.item( 0 );
        //                if ( e == null )
        //                {
        //                    return null;
        //                }
        //            }
        //
        //            val = e.getTextContent()
        //                   .trim();
        //        }
        //

        //        logger.info( "Resolving expressions in: '{}'", val );
        if ( getProfileId() == null )
        {
            return pomView.resolveExpressions( val );
        }
        else
        {
            return pomView.resolveExpressions( val, getProfileId() );
        }
    }

    protected Node getNode( final String path )
    {
        return (Node) elementContext.selectSingleNode( path );
        //        final String[] names = path.split( "/" );
        //        Element e = element;
        //        for ( final String named : names )
        //        {
        //            if ( e == null )
        //            {
        //                break;
        //            }
        //
        //            final NodeList matches = e.getElementsByTagName( named );
        //            if ( matches == null || matches.getLength() < 1 )
        //            {
        //                return null;
        //            }
        //
        //            e = (Element) matches.item( 0 );
        //        }
        //
        //        return e;
    }

    public String toXML()
    {
        return pomView.toXML( element );
    }

}
