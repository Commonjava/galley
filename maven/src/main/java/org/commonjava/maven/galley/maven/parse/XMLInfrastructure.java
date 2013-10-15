package org.commonjava.maven.galley.maven.parse;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.maven.galley.model.Transfer;
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

    private final XMLInputFactory safeInputFactory;

    public XMLInfrastructure()
    {
        safeInputFactory = XMLInputFactory.newInstance();
        safeInputFactory.setProperty( XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false );
        safeInputFactory.setProperty( XMLInputFactory.IS_VALIDATING, false );

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

    public XMLEventReader createSafeXMLEventReader( final InputStream stream )
        throws GalleyMavenXMLException
    {
        try
        {
            byte[] xml = IOUtils.toByteArray( stream );
            xml = fixUglyXML( xml );
            return safeInputFactory.createXMLEventReader( new ByteArrayInputStream( xml ) );
        }
        catch ( final XMLStreamException | IOException e )
        {
            throw new GalleyMavenXMLException( "Failed to create XMLEventReader: %s", e, e.getMessage() );
        }
    }

    public XMLStreamReader createXMLStreamReader( final InputStream stream )
        throws GalleyMavenXMLException
    {
        try
        {
            return inputFactory.createXMLStreamReader( stream );
        }
        catch ( final XMLStreamException e )
        {
            throw new GalleyMavenXMLException( "Failed to create XMLEventReader: %s", e, e.getMessage() );
        }
    }

    public XMLStreamReader createSafeXMLStreamReader( final InputStream stream )
        throws GalleyMavenXMLException
    {
        try
        {
            byte[] xml = IOUtils.toByteArray( stream );
            xml = fixUglyXML( xml );
            return safeInputFactory.createXMLStreamReader( new ByteArrayInputStream( xml ) );
        }
        catch ( final XMLStreamException | IOException e )
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
        catch ( GalleyMavenXMLException | SAXException | IOException e )
        {
            logger.debug( "Failed to parse: %s. DOM error: %s. Trying STaX parse with IS_REPLACING_ENTITY_REFERENCES == false...", e, docSource,
                          e.getMessage() );
            try
            {
                closeQuietly( stream );

                xml = fixUglyXML( xml );

                final XMLEventReader eventReader = safeInputFactory.createXMLEventReader( new ByteArrayInputStream( xml ) );
                final StAXSource source = new StAXSource( eventReader );
                final DOMResult result = new DOMResult();

                final Transformer transformer = newTransformer();
                transformer.transform( source, result );

                doc = (Document) result.getNode();
            }
            catch ( TransformerException | XMLStreamException | IOException e1 )
            {
                throw new GalleyMavenXMLException( "Failed to parse: %s. STaX error: %s.\nOriginal DOM error: %s", e1, docSource, e1.getMessage(),
                                                   e.getMessage() );
            }
        }

        return doc;
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

}
