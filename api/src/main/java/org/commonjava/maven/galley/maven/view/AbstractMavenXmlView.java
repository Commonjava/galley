package org.commonjava.maven.galley.maven.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractMavenXmlView<T extends ProjectRef>
{

    private final List<DocRef<T>> stack;

    private final XPath xpath;

    private StringSearchInterpolator ssi;

    private final Map<String, XPathExpression> xpaths = new HashMap<>();

    protected AbstractMavenXmlView( final List<DocRef<T>> stack )
    {
        this.stack = stack;
        this.xpath = XPathFactory.newInstance()
                                 .newXPath();
    }

    public List<DocRef<T>> getDocRefStack()
    {
        return stack;
    }

    public String resolveMavenExpression( final String expression )
        throws GalleyMavenException
    {
        String value = resolveXPathExpression( expression.replace( '.', '/' ), -1 );
        if ( value == null )
        {
            value = resolveXPathExpression( "//properties/" + expression, -1 );
        }

        return value;
    }

    public String resolveXPathExpression( String path, final int maxAncestry )
        throws GalleyMavenException
    {
        if ( !path.endsWith( "/text()" ) )
        {
            path += "/text()";
        }

        final Node result = resolveXPathToNode( path, maxAncestry );
        if ( result != null && result.getNodeType() == Node.TEXT_NODE )
        {
            return resolveExpressions( result.getTextContent()
                                             .trim() );
        }

        return null;
    }

    public List<String> resolveXPathExpressionToList( String path, final int maxAncestry )
        throws GalleyMavenException
    {
        if ( !path.endsWith( "/text()" ) )
        {
            path += "/text()";
        }

        final List<Node> nodes = resolveXPathToNodeList( path, maxAncestry );
        final List<String> result = new ArrayList<>( nodes.size() );
        for ( final Node node : nodes )
        {
            if ( node != null && node.getNodeType() == Node.TEXT_NODE )
            {
                result.add( resolveExpressions( node.getTextContent()
                                                    .trim() ) );
            }
        }

        return result;
    }

    public synchronized Node resolveXPathToNode( final String path, final int maxAncestry )
        throws GalleyMavenException
    {
        try
        {
            XPathExpression expression = xpaths.get( path );
            if ( expression == null )
            {
                expression = xpath.compile( path );
                xpaths.put( path, expression );
            }

            int ancestryDepth = 0;
            Node result = null;
            for ( final DocRef<T> dr : stack )
            {
                if ( maxAncestry > -1 && ancestryDepth > maxAncestry )
                {
                    break;
                }

                if ( result != null )
                {
                    break;
                }

                result = (Node) expression.evaluate( dr.getDoc(), XPathConstants.NODE );
                ancestryDepth++;
            }

            return result;
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public synchronized List<Node> resolveXPathToNodeList( final String path, final int maxAncestry )
        throws GalleyMavenException
    {
        try
        {
            XPathExpression expression = xpaths.get( path );
            if ( expression == null )
            {
                expression = xpath.compile( path );
                xpaths.put( path, expression );
            }

            int ancestryDepth = 0;
            final List<Node> result = new ArrayList<>();
            for ( final DocRef<T> dr : stack )
            {
                if ( maxAncestry > -1 && ancestryDepth > maxAncestry )
                {
                    break;
                }

                final NodeList nl = (NodeList) expression.evaluate( dr.getDoc(), XPathConstants.NODESET );
                if ( nl != null )
                {
                    for ( int i = 0; i < nl.getLength(); i++ )
                    {
                        result.add( nl.item( i ) );
                    }
                }

                ancestryDepth++;
            }

            return result;
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public String resolveXPathExpressionFrom( final Node root, String path )
        throws GalleyMavenException
    {
        if ( !path.endsWith( "/text()" ) )
        {
            path += "/text()";
        }

        final Node result = resolveXPathToNodeFrom( root, path );
        if ( result != null && result.getNodeType() == Node.TEXT_NODE )
        {
            return resolveExpressions( result.getTextContent()
                                             .trim() );
        }

        return null;
    }

    public List<String> resolveXPathExpressionToListFrom( final Node root, String path )
        throws GalleyMavenException
    {
        if ( !path.endsWith( "/text()" ) )
        {
            path += "/text()";
        }

        final List<Node> nodes = resolveXPathToNodeListFrom( root, path );
        final List<String> result = new ArrayList<>( nodes.size() );
        for ( final Node node : nodes )
        {
            if ( node != null && node.getNodeType() == Node.TEXT_NODE )
            {
                result.add( resolveExpressions( node.getTextContent()
                                                    .trim() ) );
            }
        }

        return result;
    }

    public synchronized Node resolveXPathToNodeFrom( final Node root, final String path )
        throws GalleyMavenException
    {
        try
        {
            XPathExpression expression = xpaths.get( path );
            if ( expression == null )
            {
                expression = xpath.compile( path );
                xpaths.put( path, expression );
            }

            return (Node) expression.evaluate( root, XPathConstants.NODE );
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public synchronized List<Node> resolveXPathToNodeListFrom( final Node root, final String path )
        throws GalleyMavenException
    {
        try
        {
            XPathExpression expression = xpaths.get( path );
            if ( expression == null )
            {
                expression = xpath.compile( path );
                xpaths.put( path, expression );
            }

            final List<Node> result = new ArrayList<>();
            final NodeList nl = (NodeList) expression.evaluate( root, XPathConstants.NODESET );
            if ( nl != null )
            {
                for ( int i = 0; i < nl.getLength(); i++ )
                {
                    result.add( nl.item( i ) );
                }
            }

            return result;
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public String resolveExpressions( final String value )
        throws GalleyMavenException
    {
        synchronized ( this )
        {
            if ( ssi == null )
            {
                ssi = new StringSearchInterpolator();
                ssi.addValueSource( new MavenPomViewVS<T>( this ) );
            }
        }

        try
        {
            return ssi.interpolate( value );
        }
        catch ( final InterpolationException e )
        {
            throw new GalleyMavenException( "Failed to interpolate expressions in: '%s'. Reason: %s", e, value, e.getMessage() );
        }
    }

    private static final class MavenPomViewVS<T extends ProjectRef>
        implements ValueSource
    {

        private final AbstractMavenXmlView<T> view;

        private final List<Object> feedback = new ArrayList<>();

        public MavenPomViewVS( final AbstractMavenXmlView<T> view )
        {
            this.view = view;
        }

        @Override
        public void clearFeedback()
        {
            feedback.clear();
        }

        @SuppressWarnings( "rawtypes" )
        @Override
        public List getFeedback()
        {
            return feedback;
        }

        @Override
        public Object getValue( final String expr )
        {
            try
            {
                return view.resolveMavenExpression( expr );
            }
            catch ( final GalleyMavenException e )
            {
                feedback.add( String.format( "Error resolving maven expression: '%s'", expr ) );
                feedback.add( e );
            }

            return expr;
        }

    }

}
