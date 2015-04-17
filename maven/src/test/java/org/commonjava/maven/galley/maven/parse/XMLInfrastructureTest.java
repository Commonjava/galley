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
package org.commonjava.maven.galley.maven.parse;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLInfrastructureTest
{

    protected String getBaseResource()
    {
        return "xml/";
    }

    @Test
    public void parsePOMThenDumpParentNodeBackToXML()
        throws Exception
    {
        final Document doc = loadDocument( "pom-with-parent.xml" );
        Node parent = doc.getDocumentElement().getElementsByTagName("parent").item(0);
        
        String xml = new XMLInfrastructure().toXML(parent, false);
        System.out.println( xml );
        assertThat( xml, notNullValue() );
    }

    @Test
    public void parseParentRef()
        throws Exception
    {
        final Document doc = loadDocument( "pom-with-parent.xml" );
        final ProjectVersionRef parentRef = new XMLInfrastructure().getParentRef( doc );

        assertThat( parentRef, notNullValue() );
    }

    @Test
    public void parsePOMWithUndeclaredEntity()
        throws Exception
    {
        // This is to handle the plexus POMs that have &oslash; in them.
        final Document doc = loadDocument( "pom-with-undeclared-entity.xml" );

        assertThat( doc, notNullValue() );
    }

    @Test
    public void parsePOMWithoutXMLDeclaration()
        throws Exception
    {
        // This is to handle the plexus POMs that have &oslash; in them.
        final Document doc = loadDocument( "pom-without-xml-decl.xml" );

        assertThat( doc, notNullValue() );
    }

    private Document loadDocument( final String resource )
        throws Exception
    {
        final InputStream stream = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream( getBaseResource() + resource );

        return new XMLInfrastructure().parseDocument( resource, stream );
    }

}
