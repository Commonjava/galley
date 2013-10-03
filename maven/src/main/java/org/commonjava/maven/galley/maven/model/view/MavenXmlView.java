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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenXmlView<T extends ProjectRef>
{

    private static final String EXPRESSION_PATTERN = ".*\\$\\{.+\\}.*";

    //    private final Logger logger = new Logger( getClass() );

    private final List<DocRef<T>> stack;

    private final XPathManager xpath;

    private StringSearchInterpolator ssi;

    private final List<MavenXmlMixin<T>> mixins = new ArrayList<>();

    private final Set<String> localOnlyPaths;

    public MavenXmlView( final List<DocRef<T>> stack, final XPathManager xpath, final String... localOnlyPaths )
    {
        this.stack = stack;
        this.xpath = xpath;
        this.localOnlyPaths = new HashSet<>( Arrays.asList( localOnlyPaths ) );
    }

    public MavenXmlView( final List<DocRef<T>> stack, final XPathManager xpath, final Set<String> localOnlyPaths )
    {
        this.stack = stack;
        this.xpath = xpath;
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
        throws GalleyMavenException
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
        catch ( final GalleyMavenException e )
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
        throws GalleyMavenException
    {
        if ( !path.endsWith( "/text()" ) )
        {
            path += "/text()";
        }

        final List<Node> nodes = resolveXPathToAggregatedNodeList( path, cachePath, maxAncestry );
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

    public Node resolveXPathToNode( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenException
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
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }

        return result;
    }

    public synchronized List<Node> resolveXPathToAggregatedNodeList( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenException
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
            final List<Node> result = new ArrayList<>();
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
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public synchronized List<Node> resolveXPathToFirstNodeList( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenException
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
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    private List<Node> getLocalNodeList( final XPathExpression expression, final Document doc, final String path )
    {
        NodeList nl;
        try
        {
            nl = (NodeList) expression.evaluate( doc, XPathConstants.NODESET );
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }

        if ( nl != null )
        {
            final List<Node> result = new ArrayList<>();
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
        throws GalleyMavenException
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

    public synchronized Node resolveXPathToNodeFrom( final Node root, final String path, final boolean cachePath )
        throws GalleyMavenException
    {
        try
        {
            final XPathExpression expression = xpath.getXPath( path, cachePath );

            return (Node) expression.evaluate( root, XPathConstants.NODE );
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenException( "Failed to retrieve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

    public synchronized List<Node> resolveXPathToNodeListFrom( final Node root, final String path, final boolean cachePath )
        throws GalleyMavenException
    {
        try
        {
            final XPathExpression expression = xpath.getXPath( path, cachePath );

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

    public boolean containsExpression( final String value )
    {
        return value != null && value.matches( EXPRESSION_PATTERN );
    }

    public String resolveExpressions( final String value, final String... activeProfileIds )
        throws GalleyMavenException
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
            throw new GalleyMavenException( "Failed to interpolate expressions in: '%s'. Reason: %s", e, value, e.getMessage() );
        }
    }

    private static final class MavenPomViewVS<T extends ProjectRef>
        implements ValueSource
    {

        //        private final Logger logger = new Logger( getClass() );

        private final MavenXmlView<T> view;

        private final List<Object> feedback = new ArrayList<>();

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

}
