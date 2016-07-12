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
import java.util.List;

public abstract class AbstractMavenElementView<T extends MavenXmlView<?>>
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    protected final Element element;

    protected final JXPathContext elementContext;

    protected final T xmlView;

    // all the overlapping elements found in current, parent, grandparent, ancestor poms, default is empty.
    protected ArrayList<Element> elements;

    public AbstractMavenElementView( final T xmlView, final Element element )
    {
        this.xmlView = xmlView;
        this.element = element;
        this.elementContext = JXPathUtils.newContext( element );
    }

    public final Element getElement()
    {
        if ( isElementsEmpty() )
        {
            return element;
        }
        else
        {
            return getCollapsedElement();
        }
    }

    protected Element getCollapsedElement()
    {
        Element result = element;
        String textContent = getFirstValueInOverlappingElements( result );
        if ( null != textContent )
        {
            result.setTextContent( textContent );
        }
        else
        {
            NodeList nodeList = result.getChildNodes();
            for ( int i = 0; i <= nodeList.getLength(); i++ )
            {
                Node node = nodeList.item( i );
                Element ele = node instanceof Element ? (Element) node : null;
                if ( null == ele )
                {
                    continue;
                }
                String childText = getFirstValueInOverlappingElements( ele );
                if ( null != childText )
                {
                    node.setTextContent( childText );
                }
            }
            addToCollapsedChildElements( result, 0 );
        }
        return result;
    }

    public void addElements( Element element )
    {
        elements.add( element );
    }

    public boolean isElementsEmpty()
    {
        if ( null == elements || elements.isEmpty() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public final T getXmlView()
    {
        return xmlView;
    }

    protected String getValue( final String path )
    {
        final Node node = xmlView.resolveXPathToNodeFrom( JXPathUtils.newContext( getElement() ), path, false );
        if ( node == null )
        {
            return null;
        }
        String reTextContent = getFirstValueInOverlappingElements( node );
        if ( null != reTextContent )
        {
            return reTextContent;
        }
        return node.getTextContent().trim();
    }

    protected final Element getElement( final String path )
    {
        if ( path.contains( "/" ) )
        {
            return (Element) getNode( path );
        }
        else
        {
            final NodeList nl = getElement().getElementsByTagName( path );
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
            final NodeList nl = getElement().getChildNodes();
            final List<Element> elements = new ArrayList<Element>();
            for ( int i = 0; i < nl.getLength(); i++ )
            {
                final Node node = nl.item( i );
                if ( node.getNodeType() == Node.ELEMENT_NODE )
                {
                    final Element e = (Element) node;
                    if ( "*".equals( path ) || e.getTagName().equals( path ) )
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
        Node node = (Node) JXPathUtils.newContext( getElement() ).selectSingleNode( path );
        String textContent = getFirstValueInOverlappingElements( node );
        if ( textContent != null )
        {
            node.setTextContent( textContent );
        }
        return node;
    }

    @SuppressWarnings( "unchecked" )
    protected final List<Node> getNodes( final String path )
    {
        List<Node> nodes = JXPathUtils.newContext( getElement() ).selectNodes( path );
        for ( Node node : nodes )
        {
            String textContent = getFirstValueInOverlappingElements( node );
            if ( textContent != null )
            {
                node.setTextContent( textContent );
            }
        }
        return nodes;
    }

    public final String toXML()
    {
        return xmlView.toXML( element );
    }

    protected void addAsOverlappingElements( List<? extends AbstractMavenElementView> list )
    {
        for ( AbstractMavenElementView dv : list )
        {
            if ( this.equals( dv ) )
            {
                if ( dv.isElementsEmpty() )
                {
                    dv.elements = new ArrayList<Element>();
                    dv.addElements( dv.getElement() );
                }
                dv.addElements( this.getElement() );
                break;
            }
        }
    }

    private String getFirstValueInOverlappingElements( Node child )
    {
        if ( null == child || isElementsEmpty() )
        {
            return null;
        }

        String nodeName = child.getNodeName();
        for ( Element e : elements )
        {
            NodeList nodeList = e.getChildNodes();
            for ( int i = 0; i <= nodeList.getLength(); i++ )
            {
                Node node = nodeList.item( i );
                Element ele = node instanceof Element ? (Element) node : null;
                if ( null == ele )
                {
                    continue;
                }
                String textContent = ele.getTextContent().trim();
                if ( ele.getNodeName().equals( nodeName ) && !textContent.isEmpty() )
                {
                    return textContent;
                }
            }
        }
        return null;
    }

    private Element addToCollapsedChildElements( Element current, int step )
    {
        List<Element> parents = elements;
        while ( !isElementsEmpty() && step < parents.size() - 1 )
        {
            NodeList originalElementChildNodeList = current.getChildNodes();
            List<String> originalElementChildNameList = new ArrayList<String>();
            for ( int i = 0; i <= originalElementChildNodeList.getLength(); i++ )
            {
                Node node = originalElementChildNodeList.item( i );
                Element ele = node instanceof Element ? (Element) node : null;
                if ( null == ele )
                {
                    continue;
                }
                originalElementChildNameList.add( ele.getNodeName() );
            }

            NodeList nodeList = parents.get( step + 1 ).getChildNodes();
            for ( int i = 0; i <= nodeList.getLength(); i++ )
            {
                Node node = nodeList.item( i );
                Element ele = node instanceof Element ? (Element) node : null;
                if ( null == ele )
                {
                    continue;
                }
                if ( !originalElementChildNameList.contains( ele.getNodeName() ) )
                {
                    Document dom = current.getOwnerDocument();
                    Element child = dom.createElement( ele.getNodeName() );
                    child.setTextContent( ele.getTextContent() );
                    current.appendChild( child );
                }
            }
            step++;
            addToCollapsedChildElements( current, step );
        }
        return current;
    }
}
