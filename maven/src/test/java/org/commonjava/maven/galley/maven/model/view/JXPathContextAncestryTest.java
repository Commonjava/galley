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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JXPathContextAncestryTest
{
    @Test
    @Ignore
    public void basicJXPathTest()
        throws Exception
    {
        final InputStream is = Thread.currentThread()
                                     .getContextClassLoader()
                                     .getResourceAsStream( "jxpath/simple.pom.xml" );

        final Document document = DocumentBuilderFactory.newInstance()
                                                        .newDocumentBuilder()
                                                        .parse( is );

        final JXPathContext ctx = JXPathContext.newContext( document );

        document.getDocumentElement()
                .removeAttribute( "xmlns" );

        final String projectGroupIdPath = "ancestor::project/groupId";

        // NOT what's failing...just populating the node set to traverse in order to feed the ancestor:: axis test.
        final List<?> nodes = ctx.selectNodes( "/project/dependencies/dependency" );
        for ( final Object object : nodes )
        {
            final Node node = (Node) object;
            dump( node );

            final Stack<Node> revPath = new Stack<>();

            Node parent = node;
            while ( parent != null )
            {
                revPath.push( parent );
                parent = parent.getParentNode();
            }

            JXPathContext nodeCtx = null;
            while ( !revPath.isEmpty() )
            {
                final Node part = revPath.pop();
                if ( nodeCtx == null )
                {
                    nodeCtx = JXPathContext.newContext( part );
                }
                else
                {
                    nodeCtx = JXPathContext.newContext( nodeCtx, part );
                }
            }

            System.out.println( "Path derived from context: '" + Objects.requireNonNull( nodeCtx ).getNamespaceContextPointer()
                                                                        .asPath() + "'" );

            // brute-force approach...try to force population of the parent pointers by painstakingly constructing contexts for all intermediate nodes.
            System.out.println( "Selecting groupId for declaring project using path-derived context..." );
            System.out.println( nodeCtx.getValue( projectGroupIdPath ) );

            // Naive approach...this has all the context info it needs to get parent contexts up to and including the document!
            System.out.println( "Selecting groupId for declaring project using non-derived context..." );
            System.out.println( JXPathContext.newContext( node )
                                             .getValue( projectGroupIdPath ) );
        }
    }

    private DocumentBuilder docBuilder;

    private Transformer transformer;

    @Before
    public void setup()
        throws Exception
    {
        docBuilder = DocumentBuilderFactory.newInstance()
                                           .newDocumentBuilder();

        transformer = TransformerFactory.newInstance()
                                        .newTransformer();

        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
    }

    protected void dump( final Node node )
        throws Exception
    {
        if ( node == null )
        {
            System.out.println( "Cannot dump null node." );
            return;
        }

        final StringWriter sw = new StringWriter();

        transformer.transform( new DOMSource( docBuilder.newDocument()
                                                        .importNode( node, true ) ), new StreamResult( sw ) );

        System.out.println( sw );
    }
}
