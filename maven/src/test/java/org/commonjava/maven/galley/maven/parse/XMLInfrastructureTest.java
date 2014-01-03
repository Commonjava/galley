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
package org.commonjava.maven.galley.maven.parse;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.apache.log4j.Level;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.util.logging.Log4jUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class XMLInfrastructureTest
{

    @BeforeClass
    public static void startLogging()
    {
        Log4jUtil.configure( Level.DEBUG );
    }

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

    private Document loadDocument( final String resource )
        throws Exception
    {
        final InputStream stream = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream( getBaseResource() + resource );

        return new XMLInfrastructure().parseDocument( resource, stream );
    }

}
