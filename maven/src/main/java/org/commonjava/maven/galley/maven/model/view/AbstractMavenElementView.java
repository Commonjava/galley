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

import org.apache.commons.jxpath.JXPathContext;
import org.commonjava.maven.galley.maven.parse.JXPathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMavenElementView<T extends MavenXmlView<?>>
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    protected Element element;

    protected final JXPathContext elementContext;

    protected final T xmlView;

    protected ArrayList<Element> elements;

    public AbstractMavenElementView( final T xmlView, final Element element )
    {
        this.xmlView = xmlView;
        this.element = element;
        this.elementContext = JXPathUtils.newContext( element );
    }

    public Element getElement()
    {
        return element;
    }

    public void addElement( Element element )
    {
        if ( null == elements || elements.isEmpty() )
        {
            elements = new ArrayList<Element>();
            elements.add( this.element );
            elements.add( element );
        }
        else
        {
            elements.add( element );
        }
        appendElement();
    }

    public final T getXmlView()
    {
        return xmlView;
    }

    protected String getValue( final String path )
    {
        final Node node = xmlView.resolveXPathToNodeFrom( elementContext, path, false );
        if ( node == null )
        {
            return null;
        }

        return node.getTextContent()
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
            }

            return elements;
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

    private void appendElement()
    {
        Map<String, Element> seen = new HashMap<String, Element>();
        NodeList nodeList = element.getChildNodes();
        for ( int i = 0; i <= nodeList.getLength(); i++ )
        {
            Node node = nodeList.item( i );
            Element e = node instanceof Element ? (Element) node : null;
            if ( null == e )
            {
                continue;
            }
            seen.put( e.getNodeName(), e );
        }

        for ( int i = 1; i < elements.size(); i++ )
        {
            NodeList addList = elements.get( i ).getChildNodes();

            for ( int j = 0; j < addList.getLength(); j++ )
            {
                Node node = addList.item( j );
                Element e = node instanceof Element ? (Element) node : null;
                if ( null == e )
                {
                    continue;
                }

                Document document = element.getOwnerDocument();
                Element child = document.createElement( e.getNodeName() );
                child.setTextContent( e.getTextContent() );

                if ( !seen.keySet().contains( child.getNodeName() ) )
                {
                    element.appendChild( child );
                }
                else if ( seen.get( child.getNodeName() ).getTextContent().isEmpty() )
                {
                    element.removeChild( seen.get( child.getNodeName() ) );
                    element.appendChild( child );
                }
                seen.put( child.getNodeName(), child );
            }
        }
    }
}
