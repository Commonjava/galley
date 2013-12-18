package org.commonjava.maven.galley.maven.parse;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.util.logging.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@ApplicationScoped
public class XMLInfrastructure
{

    private final Logger logger = new Logger( getClass() );

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
        safeInputFactory = XMLInputFactory.newInstance();
        safeInputFactory.setProperty( XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false );
        safeInputFactory.setProperty( XMLInputFactory.IS_VALIDATING, false );

        dbFactory = DocumentBuilderFactory.newInstance();

        // TODO: Probably don't need these available, since it's unlikely Maven can do much with them.
        dbFactory.setValidating( false );
        dbFactory.setXIncludeAware( false );
        dbFactory.setNamespaceAware( false );

        // TODO: Are these wise?? We're mainly interested in harvesting POM information, not preserving fidelity...
        dbFactory.setIgnoringComments( true );
        dbFactory.setExpandEntityReferences( false );
        dbFactory.setCoalescing( true );

        transformerFactory = TransformerFactory.newInstance();
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
            throw new GalleyMavenXMLException( "Failed to create DocumentBuilder: %s", e, e.getMessage() );
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
            throw new GalleyMavenXMLException( "Failed to create Transformer: %s", e, e.getMessage() );
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
            throw new GalleyMavenRuntimeException( "Failed to render to XML: %s. Reason: %s", e, node, e.getMessage() );
        }
        catch ( final DOMException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to render to XML: %s. Reason: %s", e, node, e.getMessage() );
        }
        catch ( final ParserConfigurationException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to render to XML: %s. Reason: %s", e, node, e.getMessage() );
        }

        return result;
    }

    public Document parseDocument( final Object docSource, final InputStream stream )
        throws GalleyMavenXMLException
    {
        if ( stream == null )
        {
            throw new GalleyMavenXMLException( "Cannot parse null input stream from: %s.", docSource );
        }

        byte[] xml;
        try
        {
            xml = IOUtils.toByteArray( stream );
        }
        catch ( final IOException e )
        {
            throw new GalleyMavenXMLException( "Failed to read raw data from XML stream: %s", e, e.getMessage() );
        }

        Document doc = null;
        try
        {
            doc = newDocumentBuilder().parse( new ByteArrayInputStream( xml ) );
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

    private Document fallbackParseDocument( byte[] xml, final Object docSource, final Exception e )
        throws GalleyMavenXMLException
    {
        logger.debug( "Failed to parse: %s. DOM error: %s. Trying STaX parse with IS_REPLACING_ENTITY_REFERENCES == false...", e, docSource,
                      e.getMessage() );
        try
        {
            xml = fixUglyXML( xml );

            final XMLEventReader eventReader = safeInputFactory.createXMLEventReader( new ByteArrayInputStream( xml ) );
            final StAXSource source = new StAXSource( eventReader );
            final DOMResult result = new DOMResult();

            final Transformer transformer = newTransformer();
            transformer.transform( source, result );

            return (Document) result.getNode();
        }
        catch ( final TransformerException e1 )
        {
            throw new GalleyMavenXMLException( "Failed to parse: %s. STaX error: %s.\nOriginal DOM error: %s", e1, docSource, e1.getMessage(),
                                               e.getMessage() );
        }
        catch ( final XMLStreamException e1 )
        {
            throw new GalleyMavenXMLException( "Failed to parse: %s. STaX error: %s.\nOriginal DOM error: %s", e1, docSource, e1.getMessage(),
                                               e.getMessage() );
        }
        catch ( final IOException e1 )
        {
            throw new GalleyMavenXMLException( "Failed to parse: %s. STaX error: %s.\nOriginal DOM error: %s", e1, docSource, e1.getMessage(),
                                               e.getMessage() );
        }
    }

    private byte[] fixUglyXML( final byte[] xml )
        throws IOException
    {
        byte[] result = xml;

        final BufferedReader br = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( result ) ) );
        final String firstLine = br.readLine()
                                   .trim();
        if ( !firstLine.startsWith( "<?xml" ) )
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream pw = new PrintStream( baos );
            pw.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
            pw.println();
            pw.println( firstLine );
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                pw.println( line );
            }

            result = baos.toByteArray();
        }

        return result;
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
            throw new GalleyMavenXMLException( "Failed to read: %s. Reason: %s", e, transfer, e.getMessage() );
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
                logger.info( "No parent declaration." );
                return null;
            }

            final Element parent = (Element) nl.item( 0 );
            gid = getChildText( "groupId", parent );
            ver = getChildText( "version", parent );
        }

        if ( isEmpty( gid ) || isEmpty( aid ) || isEmpty( ver ) )
        {
            throw new GalleyMavenXMLException( "Project GAV is invalid! (g=%s,  a=%s, v=%s)", gid, aid, ver );
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
            logger.info( "No parent declaration." );
            return null;
        }

        final Element parent = (Element) nl.item( 0 );
        final String gid = getChildText( "groupId", parent );
        final String aid = getChildText( "artifactId", parent );
        final String ver = getChildText( "version", parent );

        if ( isEmpty( gid ) || isEmpty( aid ) || isEmpty( ver ) )
        {
            throw new GalleyMavenXMLException( "Project parent is present but invalid! (g=%s,  a=%s, v=%s)", gid, aid, ver );
        }

        return new ProjectVersionRef( gid, aid, ver );
    }

    private String getChildText( final String name, final Element parent )
    {
        final NodeList nl = parent.getElementsByTagName( name );
        if ( nl == null || nl.getLength() < 1 )
        {
            logger.info( "No element: %s in: %s", name, parent.getNodeName() );
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
