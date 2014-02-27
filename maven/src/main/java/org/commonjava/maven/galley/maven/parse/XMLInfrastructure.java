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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.maven.galley.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

@ApplicationScoped
public class XMLInfrastructure
{

    private static final Set<String> XML_ENTITIES = new HashSet<String>()
    {
        {
            add( "&quot;" );
            add( "&amp;" );
            add( "&apos;" );
            add( "&lt;" );
            add( "&gt;" );
        }

        private static final long serialVersionUID = 1L;
    };

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final DocumentBuilderFactory dbFactory;

    private final TransformerFactory transformerFactory;

    private final XMLInputFactory safeInputFactory;

    static
    {
        final Map<String, String> props = new HashMap<String, String>()
        {

            {
                put( "org.apache.xml.dtm.DTMManager", "org.apache.xml.dtm.ref.DTMManagerDefault" );
                put( "com.sun.org.apache.xml.internal.dtm.DTMManager", "com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault" );
            }

            private static final long serialVersionUID = 1L;
        };

        for ( final Entry<String, String> entry : props.entrySet() )
        {
            final String key = entry.getKey();
            final String value = entry.getValue();

            if ( System.getProperty( key ) == null )
            {
                System.setProperty( key, value );
            }
        }
    }

    public XMLInfrastructure()
    {
        transformerFactory = TransformerFactory.newInstance();

        if ( !transformerFactory.getClass()
                                .getName()
                                .contains( "redirected" ) )
        {
            safeInputFactory = XMLInputFactory.newInstance();
            safeInputFactory.setProperty( XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false );
            safeInputFactory.setProperty( XMLInputFactory.IS_VALIDATING, false );
        }
        else
        {
            logger.warn( "Somebody is playing games with the TransformerFactory...we cannot use it safely: {}", transformerFactory );
            safeInputFactory = null;
        }

        dbFactory = DocumentBuilderFactory.newInstance();

        // TODO: Probably don't need these available, since it's unlikely Maven can do much with them.
        dbFactory.setValidating( false );
        dbFactory.setXIncludeAware( false );
        dbFactory.setNamespaceAware( false );

        // TODO: Are these wise?? We're mainly interested in harvesting POM information, not preserving fidelity...
        dbFactory.setIgnoringComments( true );
        dbFactory.setExpandEntityReferences( false );
        dbFactory.setCoalescing( true );
    }

    public Element createElement( final Element below, final String relativePath, final Map<String, String> leafElements )
    {
        final Document doc = below.getOwnerDocument();

        Element insertionPoint = below;
        if ( relativePath.length() > 0 && !"/".equals( relativePath ) )
        {
            final String[] intermediates = relativePath.split( "/" );

            // DO NOT traverse last "intermediate"...this will be the new element!
            for ( int i = 0; i < intermediates.length - 1; i++ )
            {
                final NodeList nl = insertionPoint.getElementsByTagNameNS( below.getNamespaceURI(), intermediates[i] );
                if ( nl != null && nl.getLength() > 0 )
                {
                    insertionPoint = (Element) nl.item( 0 );
                }
                else
                {
                    final Element e = doc.createElementNS( below.getNamespaceURI(), intermediates[i] );
                    insertionPoint.appendChild( e );
                    insertionPoint = e;
                }
            }

            final Element e = doc.createElementNS( below.getNamespaceURI(), intermediates[intermediates.length - 1] );
            insertionPoint.appendChild( e );
            insertionPoint = e;
        }

        for ( final Entry<String, String> entry : leafElements.entrySet() )
        {
            final String key = entry.getKey();
            final String value = entry.getValue();

            final Element e = doc.createElementNS( below.getNamespaceURI(), key );
            insertionPoint.appendChild( e );
            e.setTextContent( value );
        }

        return insertionPoint;
    }

    public DocumentBuilder newDocumentBuilder()
        throws GalleyMavenXMLException
    {
        try
        {
            return dbFactory.newDocumentBuilder();
        }
        catch ( final ParserConfigurationException e )
        {
            throw new GalleyMavenXMLException( "Failed to create DocumentBuilder: {}", e, e.getMessage() );
        }
    }

    public Transformer newTransformer()
        throws GalleyMavenXMLException
    {
        try
        {

            return transformerFactory.newTransformer();
        }
        catch ( final TransformerConfigurationException e )
        {
            throw new GalleyMavenXMLException( "Failed to create Transformer: {}", e, e.getMessage() );
        }
    }

    public String toXML( final Node node )
    {
        String result = null;
        try
        {
            final StringWriter sw = new StringWriter();
            final Transformer transformer = transformerFactory.newTransformer();
            final DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();

            transformer.transform( new DOMSource( docBuilder.newDocument()
                                                            .importNode( node, true ) ), new StreamResult( sw ) );

            result = sw.toString();
        }
        catch ( final TransformerException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to render to XML: {}. Reason: {}", e, node, e.getMessage() );
        }
        catch ( final DOMException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to render to XML: {}. Reason: {}", e, node, e.getMessage() );
        }
        catch ( final ParserConfigurationException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to render to XML: {}. Reason: {}", e, node, e.getMessage() );
        }

        return result;
    }

    public Document parseDocument( final Object docSource, final InputStream stream )
        throws GalleyMavenXMLException
    {
        if ( stream == null )
        {
            throw new GalleyMavenXMLException( "Cannot parse null input stream from: {}.", docSource );
        }

        String xml;
        try
        {
            xml = IOUtils.toString( stream );
        }
        catch ( final IOException e )
        {
            throw new GalleyMavenXMLException( "Failed to read raw data from XML stream: {}", e, e.getMessage() );
        }

        Document doc = null;
        try
        {
            doc = newDocumentBuilder().parse( new InputSource( new StringReader( xml ) ) );
        }
        catch ( final GalleyMavenXMLException e )
        {
            closeQuietly( stream );
            doc = fallbackParseDocument( xml, docSource, e );
        }
        catch ( final SAXException e )
        {
            closeQuietly( stream );
            doc = fallbackParseDocument( xml, docSource, e );
        }
        catch ( final IOException e )
        {
            closeQuietly( stream );
            doc = fallbackParseDocument( xml, docSource, e );
        }

        return doc;
    }

    private Document fallbackParseDocument( String xml, final Object docSource, final Exception e )
        throws GalleyMavenXMLException
    {
        logger.debug( "Failed to parse: {}. DOM error: {}. Trying STaX parse with IS_REPLACING_ENTITY_REFERENCES == false...", e, docSource,
                      e.getMessage() );
        try
        {
            Source source;

            if ( safeInputFactory != null )
            {
                xml = repairXmlDeclaration( xml );

                final XMLEventReader eventReader = safeInputFactory.createXMLEventReader( new StringReader( xml ) );
                source = new StAXSource( eventReader );
            }
            else
            {
                // Deal with &oslash; and other undeclared entities...
                xml = escapeNonXMLEntityRefs( xml );

                final XMLReader reader = XMLReaderFactory.createXMLReader();
                reader.setFeature( "http://xml.org/sax/features/validation", false );

                source = new SAXSource( reader, new InputSource( new StringReader( xml ) ) );
            }

            final DOMResult result = new DOMResult();

            final Transformer transformer = newTransformer();
            transformer.transform( source, result );

            return (Document) result.getNode();
        }
        catch ( final TransformerException e1 )
        {
            throw new GalleyMavenXMLException( "Failed to parse: {}. Transformer error: {}.\nOriginal DOM error: {}", e1, docSource, e1.getMessage(),
                                               e.getMessage() );
        }
        catch ( final SAXException e1 )
        {
            throw new GalleyMavenXMLException( "Failed to parse: {}. SAX error: {}.\nOriginal DOM error: {}", e1, docSource, e1.getMessage(),
                                               e.getMessage() );
        }
        catch ( final XMLStreamException e1 )
        {
            throw new GalleyMavenXMLException( "Failed to parse: {}. STaX error: {}.\nOriginal DOM error: {}", e1, docSource, e1.getMessage(),
                                               e.getMessage() );
        }
    }

    private String repairXmlDeclaration( final String xml )
    {
        if ( !xml.startsWith( "<?xml" ) )
        {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml;
        }

        return xml;
    }

    private String escapeNonXMLEntityRefs( final String xml )
    {
        final Matcher m = Pattern.compile( "&([^\\s;]+;)" )
                                 .matcher( xml );

        final StringBuffer sb = new StringBuffer();
        while ( m.find() )
        {
            String value = m.group();
            if ( !XML_ENTITIES.contains( value ) )
            {
                value = "&amp;" + m.group( 1 );
            }

            m.appendReplacement( sb, value );
        }

        m.appendTail( sb );

        return sb.toString();
    }

    public Document parse( final Transfer transfer )
        throws GalleyMavenXMLException
    {
        InputStream stream = null;
        Document doc = null;
        try
        {
            try
            {
                stream = transfer.openInputStream( false );
                doc = parseDocument( transfer.toString(), stream );
            }
            catch ( final GalleyMavenXMLException e )
            {
            }
        }
        catch ( final IOException e )
        {
            throw new GalleyMavenXMLException( "Failed to read: {}. Reason: {}", e, transfer, e.getMessage() );
        }
        finally
        {
            closeQuietly( stream );
        }

        return doc;
    }

    public ProjectVersionRef getProjectVersionRef( final Document doc )
        throws GalleyMavenXMLException
    {
        final Element project = doc.getDocumentElement();

        String gid = getChildText( "groupId", project );
        final String aid = getChildText( "artifactId", project );
        String ver = getChildText( "version", project );

        if ( isEmpty( gid ) || isEmpty( ver ) )
        {
            final NodeList nl = project.getElementsByTagName( "parent" );
            if ( nl == null || nl.getLength() < 1 )
            {
                logger.debug( "No parent declaration." );
                return null;
            }

            final Element parent = (Element) nl.item( 0 );
            gid = getChildText( "groupId", parent );
            ver = getChildText( "version", parent );
        }

        if ( isEmpty( gid ) || isEmpty( aid ) || isEmpty( ver ) )
        {
            throw new GalleyMavenXMLException( "Project GAV is invalid! (g={},  a={}, v={})", gid, aid, ver );
        }

        return new ProjectVersionRef( gid, aid, ver );
    }

    public ProjectVersionRef getParentRef( final Document doc )
        throws GalleyMavenXMLException
    {
        final Element project = doc.getDocumentElement();
        final NodeList nl = project.getElementsByTagName( "parent" );
        if ( nl == null || nl.getLength() < 1 )
        {
            logger.debug( "No parent declaration." );
            return null;
        }

        final Element parent = (Element) nl.item( 0 );
        final String gid = getChildText( "groupId", parent );
        final String aid = getChildText( "artifactId", parent );
        final String ver = getChildText( "version", parent );

        if ( isEmpty( gid ) || isEmpty( aid ) || isEmpty( ver ) )
        {
            throw new GalleyMavenXMLException( "Project parent is present but invalid! (g={},  a={}, v={})", gid, aid, ver );
        }

        return new ProjectVersionRef( gid, aid, ver );
    }

    private String getChildText( final String name, final Element parent )
    {
        final NodeList nl = parent.getElementsByTagName( name );
        if ( nl == null || nl.getLength() < 1 )
        {
            logger.debug( "No element: {} in: {}", name, parent.getNodeName() );
            return null;
        }

        Element elem = null;
        for ( int i = 0; i < nl.getLength(); i++ )
        {
            final Element e = (Element) nl.item( i );
            if ( e.getParentNode() == parent )
            {
                elem = e;
                break;
            }
        }

        return elem == null ? null : elem.getTextContent()
                                         .trim();
    }

}
