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
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.commonjava.maven.galley.maven.parse.JXPathUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractMavenElementView<T extends MavenXmlView<?>>
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    protected final Element element;

    protected final JXPathContext elementContext;

    protected final T xmlView;

    public AbstractMavenElementView( final T xmlView, final Element element )
    {
        this.xmlView = xmlView;
        this.element = element;

        this.elementContext = JXPathUtils.newContext( element );
    }

    public final Element getElement()
    {
        return element;
    }

    public final T getXmlView()
    {
        return xmlView;
    }

    protected String getValue( final String path )
    {
        final Element e = (Element) xmlView.resolveXPathToNodeFrom( elementContext, path, false );

        if ( e == null )
        {
            return null;
        }

        return e.getTextContent()
                .trim();
    }

    protected final Element getElement( final String path )
    {
        if ( path.contains( "/" ) )
        {
            return (Element) getNode( path );
        }
        else
        {
            final NodeList nl = element.getElementsByTagName( path );
            if ( nl.getLength() > 0 )
            {
                return (Element) nl.item( 0 );
            }
        }

        return null;
    }

    protected final List<Element> getElements( final String path )
    {
        if ( path.contains( "/" ) )
        {
            final List<Node> nodes = getNodes( path );
            if ( nodes != null )
            {
                final List<Element> elements = new ArrayList<Element>();
                for ( final Node node : nodes )
                {
                    elements.add( (Element) node );
                }

                return elements;
            }
        }
        else
        {
            final NodeList nl = element.getChildNodes();
            final List<Element> elements = new ArrayList<Element>();
            for ( int i = 0; i < nl.getLength(); i++ )
            {
                final Node node = nl.item( i );
                if ( node.getNodeType() == Node.ELEMENT_NODE )
                {
                    final Element e = (Element) node;
                    if ( "*".equals( path ) || e.getTagName()
                                                .equals( path ) )
                    {
                        elements.add( (Element) node );
                    }
                }

                return elements;
            }
        }

        return null;
    }

    protected final Node getNode( final String path )
    {
        return (Node) elementContext.selectSingleNode( path );
    }

    @SuppressWarnings( "unchecked" )
    protected final List<Node> getNodes( final String path )
    {
        return elementContext.selectNodes( path );
    }

    public final String toXML()
    {
        return xmlView.toXML( element );
    }

}
