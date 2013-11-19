package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenXmlView<T extends ProjectRef>
{

    private static final String EXPRESSION_PATTERN = ".*\\$\\{.+\\}.*";

    protected final Logger logger = new Logger( getClass() );

    protected final List<DocRef<T>> stack;

    protected final XPathManager xpath;

    protected StringSearchInterpolator ssi;

    protected final List<MavenXmlMixin<T>> mixins = new ArrayList<MavenXmlMixin<T>>();

    protected final Set<String> localOnlyPaths;

    protected final XMLInfrastructure xml;

    public MavenXmlView( final List<DocRef<T>> stack, final XPathManager xpath, final XMLInfrastructure xml, final String... localOnlyPaths )
    {
        this.stack = stack;
        this.xpath = xpath;
        this.xml = xml;
        this.localOnlyPaths = new HashSet<String>( Arrays.asList( localOnlyPaths ) );
    }

    public MavenXmlView( final List<DocRef<T>> stack, final XPathManager xpath, final XMLInfrastructure xml, final Set<String> localOnlyPaths )
    {
        this.stack = stack;
        this.xpath = xpath;
        this.xml = xml;
        this.localOnlyPaths = localOnlyPaths;
    }

    public T getRef()
    {
        return stack.get( 0 )
                    .getRef();
    }

    public List<DocRef<T>> getDocRefStack()
    {
        return stack;
    }

    public String resolveMavenExpression( final String expression, final String... activeProfileIds )
        throws GalleyMavenException
    {
        String expr = expression.replace( '.', '/' );
        if ( !expr.startsWith( "/" ) )
        {
            expr = "/" + expr;
        }

        if ( !expr.startsWith( "/project" ) )
        {
            expr = "/project" + expr;
        }

        String value = resolveXPathExpression( expr, true, -1 );
        if ( value == null )
        {
            value = resolveXPathExpression( "//properties/" + expression, true, -1 );
        }

        for ( int i = 0; value == null && activeProfileIds != null && i < activeProfileIds.length; i++ )
        {
            final String profileId = activeProfileIds[i];
            value = resolveXPathExpression( "//profile[id/text()=\"" + profileId + "\"]/properties/" + expression, true, -1, activeProfileIds );
        }

        return value;
    }

    public String resolveXPathExpression( String path, final boolean cachePath, final int maxAncestry, final String... activeProfileIds )
    {
        if ( !path.endsWith( "/text()" ) )
        {
            path += "/text()";
        }

        Node result = null;
        try
        {
            result = resolveXPathToNode( path, cachePath, maxAncestry );
        }
        catch ( final GalleyMavenRuntimeException e )
        {
            // TODO: We don't want to spit this out, but is there another more appropriate action than ignoring it?
        }

        if ( result != null && result.getNodeType() == Node.TEXT_NODE )
        {
            final String raw = result.getTextContent()
                                     .trim();

            //            logger.info( "Raw content of: '%s' is: '%s'", path, raw );
            return resolveExpressions( raw, activeProfileIds );
        }

        return null;
    }

    public List<String> resolveXPathExpressionToAggregatedList( String path, final boolean cachePath, final int maxAncestry )
    {
        if ( !path.endsWith( "/text()" ) )
        {
            path += "/text()";
        }

        final List<Node> nodes = resolveXPathToAggregatedNodeList( path, cachePath, maxAncestry );
        final List<String> result = new ArrayList<String>( nodes.size() );
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

    public Node resolveXPathToNode( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenRuntimeException
    {
        Node result = null;
        try
        {
            final XPathExpression expression = xpath.getXPath( path, cachePath );

            int maxAncestry = maxDepth;
            for ( final String pathPrefix : localOnlyPaths )
            {
                if ( path.startsWith( pathPrefix ) )
                {
                    maxAncestry = 0;
                    break;
                }
            }

            int ancestryDepth = 0;
            for ( final DocRef<T> dr : stack )
            {
                if ( maxAncestry > -1 && ancestryDepth > maxAncestry )
                {
                    break;
                }

                result = (Node) expression.evaluate( dr.getDoc(), XPathConstants.NODE );
                //                logger.info( "Value of '%s' at depth: %d is: %s", path, ancestryDepth, result );

                if ( result != null )
                {
                    break;
                }

                ancestryDepth++;
            }

            if ( result == null )
            {
                for ( final MavenXmlMixin<T> mixin : mixins )
                {
                    if ( mixin.matches( path ) )
                    {
                        result = mixin.getMixin()
                                      .resolveXPathToNode( path, true, maxAncestry );
                        //                        logger.info( "Value of '%s' in mixin: %s is: '%s'", path, mixin );
                    }

                    if ( result != null )
                    {
                        break;
                    }
                }
            }
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }

        return result;
    }

    public synchronized List<Node> resolveXPathToAggregatedNodeList( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenRuntimeException
    {
        try
        {
            final XPathExpression expression = xpath.getXPath( path, cachePath );

            int maxAncestry = maxDepth;
            for ( final String pathPrefix : localOnlyPaths )
            {
                if ( path.startsWith( pathPrefix ) )
                {
                    maxAncestry = 0;
                    break;
                }
            }

            int ancestryDepth = 0;
            final List<Node> result = new ArrayList<Node>();
            for ( final DocRef<T> dr : stack )
            {
                if ( maxAncestry > -1 && ancestryDepth > maxAncestry )
                {
                    break;
                }

                final List<Node> nodes = getLocalNodeList( expression, dr.getDoc(), path );
                if ( nodes != null )
                {
                    for ( final Node node : nodes )
                    {
                        result.add( node );
                    }
                }

                ancestryDepth++;
            }

            for ( final MavenXmlMixin<T> mixin : mixins )
            {
                if ( !mixin.matches( path ) )
                {
                    continue;
                }

                final List<Node> nodes = mixin.getMixin()
                                              .resolveXPathToAggregatedNodeList( path, cachePath, maxAncestry );
                if ( nodes != null )
                {
                    for ( final Node node : nodes )
                    {
                        result.add( node );
                    }
                }
            }

            return result;
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public synchronized List<Node> resolveXPathToFirstNodeList( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenRuntimeException
    {
        try
        {
            final XPathExpression expression = xpath.getXPath( path, cachePath );

            int maxAncestry = maxDepth;
            for ( final String pathPrefix : localOnlyPaths )
            {
                if ( path.startsWith( pathPrefix ) )
                {
                    maxAncestry = 0;
                    break;
                }
            }

            int ancestryDepth = 0;
            for ( final DocRef<T> dr : stack )
            {
                if ( maxAncestry > -1 && ancestryDepth > maxAncestry )
                {
                    break;
                }

                final List<Node> result = getLocalNodeList( expression, dr.getDoc(), path );
                if ( result != null )
                {
                    return result;
                }

                ancestryDepth++;
            }

            for ( final MavenXmlMixin<T> mixin : mixins )
            {
                if ( !mixin.matches( path ) )
                {
                    continue;
                }

                final List<Node> result = mixin.getMixin()
                                               .resolveXPathToFirstNodeList( path, cachePath, maxAncestry );
                if ( result != null )
                {
                    return result;
                }
            }

            return null;
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    protected List<Node> getLocalNodeList( final XPathExpression expression, final Document doc, final String path )
        throws GalleyMavenRuntimeException
    {
        NodeList nl;
        try
        {
            nl = (NodeList) expression.evaluate( doc, XPathConstants.NODESET );
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }

        if ( nl != null && nl.getLength() > 0 )
        {
            final List<Node> result = new ArrayList<Node>();
            for ( int i = 0; i < nl.getLength(); i++ )
            {
                result.add( nl.item( i ) );
            }

            // we're not aggregating, so return the result.
            return result;
        }

        return null;
    }

    public String resolveXPathExpressionFrom( final Node root, String path )
    {
        if ( !path.endsWith( "/text()" ) )
        {
            path += "/text()";
        }

        final Node result = resolveXPathToNodeFrom( root, path, true );
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

        final List<Node> nodes = resolveXPathToNodeListFrom( root, path, true );
        final List<String> result = new ArrayList<String>( nodes.size() );
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

    public synchronized Node resolveXPathToNodeFrom( final Node root, final String path, final boolean cachePath )
    {
        try
        {
            final XPathExpression expression = xpath.getXPath( path, cachePath );

            return (Node) expression.evaluate( root, XPathConstants.NODE );
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public synchronized List<Node> resolveXPathToNodeListFrom( final Node root, final String path, final boolean cachePath )
        throws GalleyMavenRuntimeException
    {
        try
        {
            final XPathExpression expression = xpath.getXPath( path, cachePath );

            final List<Node> result = new ArrayList<Node>();
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
            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public boolean containsExpression( final String value )
    {
        return value != null && value.matches( EXPRESSION_PATTERN );
    }

    public String resolveExpressions( final String value, final String... activeProfileIds )
    {
        if ( !containsExpression( value ) )
        {
            //            logger.info( "No expressions in: '%s'", value );
            return value;
        }

        synchronized ( this )
        {
            if ( ssi == null )
            {
                ssi = new StringSearchInterpolator();
                ssi.addValueSource( new MavenPomViewVS<T>( this, activeProfileIds ) );
            }
        }

        try
        {
            String result = ssi.interpolate( value );
            //            logger.info( "Resolved '%s' to '%s'", value, result );

            if ( result == null || result.trim()
                                         .length() < 1 )
            {
                result = value;
            }

            return result;
        }
        catch ( final InterpolationException e )
        {
            logger.error( "Failed to resolve expressions in: '%s'. Reason: %s", e, value, e.getMessage() );
            return value;
        }
    }

    private static final class MavenPomViewVS<T extends ProjectRef>
        implements ValueSource
    {

        //        private final Logger logger = new Logger( getClass() );

        private final MavenXmlView<T> view;

        private final List<Object> feedback = new ArrayList<Object>();

        private final String[] activeProfileIds;

        public MavenPomViewVS( final MavenXmlView<T> view, final String[] activeProfileIds )
        {
            this.view = view;
            this.activeProfileIds = activeProfileIds;
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
                final String value = view.resolveMavenExpression( expr, activeProfileIds );
                //                logger.info( "Value of: '%s' is: '%s'", expr, value );
                return value;
            }
            catch ( final GalleyMavenException e )
            {
                feedback.add( String.format( "Error resolving maven expression: '%s'", expr ) );
                feedback.add( e );
            }

            return null;
        }

    }

    public List<MavenXmlMixin<T>> getMixins()
    {
        return mixins;
    }

    public void addMixin( final MavenXmlMixin<T> mixin )
    {
        mixins.add( mixin );
    }

    public void removeMixin( final MavenXmlMixin<T> mixin )
    {
        mixins.remove( mixin );
    }

    public String toXML( final Element element )
    {
        return xml.toXML( element );
    }

}
