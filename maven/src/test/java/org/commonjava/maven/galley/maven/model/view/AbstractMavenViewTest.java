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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMaven304PluginDefaults;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMavenPluginImplications;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class AbstractMavenViewTest
{

    private DocumentBuilder docBuilder;

    private Transformer transformer;

    private XPathManager xpath;

    private XMLInfrastructure xml;

    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    @Before
    public void setup()
        throws Exception
    {
        xml = new XMLInfrastructure();
        docBuilder = xml.newDocumentBuilder();

        transformer = xml.newTransformer();

        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
        transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );

        xpath = new XPathManager();
    }

    protected DependencyView loadFirstDirectDependency( final String... pomNames )
        throws Exception
    {
        final MavenPomView mpv = loadPoms( pomNames );

        return mpv.getAllDirectDependencies()
                  .get( 0 );
    }

    protected DependencyView loadFirstManagedDependency( final String... pomNames )
        throws Exception
    {
        final MavenPomView mpv = loadPoms( pomNames );

        return mpv.getAllManagedDependencies()
                  .get( 0 );
    }

    protected MavenPomView loadPoms( final String... pomNames )
        throws Exception
    {
        final List<DocRef<ProjectVersionRef>> stack = new ArrayList<DocRef<ProjectVersionRef>>();

        ProjectVersionRef pvr = null;
        for ( final String pomName : pomNames )
        {
            final InputStream is = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream( getBaseResource() + pomName );

            final Document document = DocumentBuilderFactory.newInstance()
                                                            .newDocumentBuilder()
                                                            .parse( is );

            final ProjectVersionRef ref = xml.getProjectVersionRef( document );
            if ( pvr == null )
            {
                pvr = ref;
            }

            final DocRef<ProjectVersionRef> dr = new DocRef<ProjectVersionRef>( ref, new SimpleLocation( "http://localhost:8080/" ), document );

            stack.add( dr );
        }

        return new MavenPomView( pvr, stack, xpath, new StandardMaven304PluginDefaults(), new StandardMavenPluginImplications( xml ), xml );
    }

    protected MavenXmlView<ProjectRef> loadDocs( final Set<String> localOnlyPaths, final String... docNames )
        throws Exception
    {
        final List<DocRef<ProjectRef>> stack = new ArrayList<DocRef<ProjectRef>>();
        final ProjectRef pr = new ProjectRef( "not.used", "project-ref" );
        for ( final String pomName : docNames )
        {
            final InputStream is = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream( getBaseResource() + pomName );

            final Document document = DocumentBuilderFactory.newInstance()
                                                            .newDocumentBuilder()
                                                            .parse( is );

            final DocRef<ProjectRef> dr = new DocRef<ProjectRef>( pr, new SimpleLocation( "http://localhost:8080/" ), document );

            stack.add( dr );
        }

        return new MavenXmlView<ProjectRef>( stack, xpath, xml, localOnlyPaths );
    }

    protected void dump( final Node node )
        throws Exception
    {
        if ( node == null )
        {
            logger.error( "Cannot dump null node." );
            return;
        }

        final StringWriter sw = new StringWriter();

        transformer.transform( new DOMSource( docBuilder.newDocument()
                                                        .importNode( node, true ) ), new StreamResult( sw ) );

        logger.info( sw.toString() );
    }

    protected abstract String getBaseResource();

}
