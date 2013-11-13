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
