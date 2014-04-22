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
package org.commonjava.maven.galley.maven.parse;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.junit.Test;
import org.w3c.dom.Document;

public class XMLInfrastructureTest
{

    protected String getBaseResource()
    {
        return "xml/";
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
