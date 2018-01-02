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

    protected Element collapsed;

    protected final JXPathContext elementContext;

    protected JXPathContext collapsedElementContext;

    protected final T xmlView;

    // the list which is awaiting the be merged to collapsed Element, and added to the overlapping elements list.
    protected ArrayList<Element> elementsAwaitingCollapse;

    // all the overlapping elements found in current, parent, grandparent, ancestor poms, default is empty.
    protected ArrayList<Element> elements;

    public AbstractMavenElementView( final T xmlView, final Element element )
    {
        this.xmlView = xmlView;
        this.element = element;
        this.elementContext = JXPathUtils.newContext( element );
        this.collapsed = element;
        this.collapsedElementContext = elementContext;
    }

    public final Element getElement()
    {
        return element;
    }

    public Element getCollapsedElement()
    {
        if ( isElementStackEmpty( elementsAwaitingCollapse ) )
        {
            return collapsed;
        }

        addElementToStack( elementsAwaitingCollapse );
        String textContent = getFirstValueInOverlappingElements( collapsed, elementsAwaitingCollapse );
        if ( null != textContent )
        {
            collapsed.setTextContent( textContent );
        }
        else
        {
            NodeList nodeList = collapsed.getChildNodes();
            for ( int i = 0; i <= nodeList.getLength(); i++ )
            {
                Node node = nodeList.item( i );
                if ( !(node instanceof Element) )
                {
                    continue;
                }
                String childText = getFirstValueInOverlappingElements( node, elementsAwaitingCollapse );
                if ( null != childText )
                {
                    node.setTextContent( childText );
                }
            }
            addToCollapsedChildElements( collapsed, 0 );
        }
        elementsAwaitingCollapse.clear();
        collapsedElementContext = JXPathUtils.newContext( collapsed );
        return collapsed;
    }

    public final T getXmlView()
    {
        return xmlView;
    }


    public final String toXML()
    {
        return xmlView.toXML( element );
    }

    protected JXPathContext getCollapsedElementContext()
    {
        getCollapsedElement();
        return collapsedElementContext;
    }

    protected String getValue( final String path )
    {
        final Node node = xmlView.resolveXPathToNodeFrom( getCollapsedElementContext(), path, false );
        if ( node == null )
        {
            return null;
        }
        String reTextContent = getFirstValueInOverlappingElements( node, elements );
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
            final NodeList nl = getCollapsedElement().getElementsByTagName( path );
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
                final List<Element> elements = new ArrayList<>();
                for ( final Node node : nodes )
                {
                    elements.add( (Element) node );
                }

                return elements;
            }
        }
        else
        {
            final NodeList nl = getCollapsedElement().getChildNodes();
            final List<Element> elements = new ArrayList<>();
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
        Node node = (Node) getCollapsedElementContext().selectSingleNode( path );
        String textContent = getFirstValueInOverlappingElements( node, elements );
        if ( textContent != null )
        {
            node.setTextContent( textContent );
        }
        return node;
    }

    @SuppressWarnings( "unchecked" )
    protected final List<Node> getNodes( final String path )
    {
        List<Node> nodes = getCollapsedElementContext().selectNodes( path );
        for ( Node node : nodes )
        {
            String textContent = getFirstValueInOverlappingElements( node, elements );
            if ( textContent != null )
            {
                node.setTextContent( textContent );
            }
        }
        return nodes;
    }


    /**
     * This is used to add overlapping parent element found when the key was matched as equal.
     *
     * @param other The one which will be appended as current's overlapping elements.
    */
    protected void addOverlappingElements( AbstractMavenElementView other)
    {
        if ( isElementStackEmpty( elements ) )
        {
            this.elements = new ArrayList<>();
        }
        addElement( other.getCollapsedElement() );
    }

    private void addElement( Element other )
    {
        if ( isElementStackEmpty( elementsAwaitingCollapse ) )
        {
            this.elementsAwaitingCollapse = new ArrayList<>();
        }
        elementsAwaitingCollapse.add( other );
    }

    /**
     * Check if there is element's content need to be replaced among the overlapping parents elements list
     * for the same original element's node name / original element's children's nodes names.
     *
     * @param child The node need to be verified if there is element's node name
     *              first matched among its overlapping parents elements.
     * @return The node's textContent value which is first matched, NULL if no matched.
     */
    private String getFirstValueInOverlappingElements( Node child, List<Element> overlappingElements )
    {
        if ( null == child || isElementStackEmpty( overlappingElements ) )
        {
            return null;
        }

        String childName = child.getNodeName();
        String childTextContent = child.getTextContent();
        for ( Element e : overlappingElements )
        {
            NodeList nodeList = e.getChildNodes();
            for ( int i = 0; i <= nodeList.getLength(); i++ )
            {
                Node node = nodeList.item( i );
                if ( !(node instanceof Element) )
                {
                    continue;
                }

                String textContent = node.getTextContent().trim();
                if ( node.getNodeName().equals( childName ) && childTextContent.isEmpty() && !textContent.isEmpty() )
                {
                    return textContent;
                }
            }
        }
        return null;
    }

    /**
     * Append new children elements to the final result(collapsed) when there are children ones found in
     * overlapping parents elements, but not found in current element children.
     *
     * @param current The element which will be collapsed during this process.
     * @param step Iteration counter.
     * @return Final collapsed element.
     */
    @SuppressWarnings( "UnusedReturnValue" )
    private Element addToCollapsedChildElements( Element current, int step )
    {
        List<Element> parents = elementsAwaitingCollapse;
        while ( !isElementStackEmpty( parents ) && step < parents.size() )
        {
            NodeList originalElementChildNodeList = current.getChildNodes();
            List<String> originalElementChildNameList = new ArrayList<>();
            for ( int i = 0; i <= originalElementChildNodeList.getLength(); i++ )
            {
                Node node = originalElementChildNodeList.item( i );
                if ( !(node instanceof Element) )
                {
                    continue;
                }
                originalElementChildNameList.add( node.getNodeName() );
            }

            NodeList nodeList = parents.get( step ).getChildNodes();
            for ( int i = 0; i <= nodeList.getLength(); i++ )
            {
                Node node = nodeList.item( i );
                if ( !(node instanceof Element) )
                {
                    continue;
                }

                if ( !originalElementChildNameList.contains( node.getNodeName() ) )
                {
                    Document dom = current.getOwnerDocument();
                    Element child = dom.createElement( node.getNodeName() );
                    child.setTextContent( node.getTextContent() );
                    current.appendChild( child );
                }
            }
            step++;
            addToCollapsedChildElements( current, step );
        }
        return current;
    }

    private void addElementToStack( List<Element> elementsAwaiting )
    {
        elements.addAll( elementsAwaiting );
    }

    private boolean isElementStackEmpty( List<Element> elements )
    {
        return null == elements || elements.isEmpty();
    }
}
