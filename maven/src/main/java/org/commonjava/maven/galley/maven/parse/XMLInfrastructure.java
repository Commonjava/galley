package org.commonjava.maven.galley.maven.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.util.logging.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@ApplicationScoped
public class XMLInfrastructure
{

    private final Logger logger = new Logger( getClass() );

    private final XMLInputFactory inputFactory;

    private final DocumentBuilderFactory dbFactory;

    private final TransformerFactory transformerFactory;

    public XMLInfrastructure()
    {
        inputFactory = XMLInputFactory.newInstance();
        logger.info( "Using XMLInputFactory: %s", inputFactory.getClass()
                                                              .getName() );

        dbFactory = DocumentBuilderFactory.newInstance();

        transformerFactory = TransformerFactory.newInstance();
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

    public void setInputFactoryProperty( final String key, final Object value )
    {
        inputFactory.setProperty( key, value );
    }

    public XMLEventReader createXMLEventReader( final InputStream stream )
        throws GalleyMavenXMLException
    {
        try
        {
            return inputFactory.createXMLEventReader( stream );
        }
        catch ( final XMLStreamException e )
        {
            throw new GalleyMavenXMLException( "Failed to create XMLEventReader: %s", e, e.getMessage() );
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
        catch ( ParserConfigurationException | DOMException | TransformerException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to render to XML: %s. Reason: %s", e, node, e.getMessage() );
        }

        return result;
    }

    public Document parseDocument( final InputStream stream )
        throws GalleyMavenXMLException
    {
        try
        {
            return newDocumentBuilder().parse( stream );
        }
        catch ( GalleyMavenXMLException | SAXException | IOException e )
        {
            throw new GalleyMavenXMLException( "Failed to parse InputStream: %s", e, e.getMessage() );
        }
    }

}
