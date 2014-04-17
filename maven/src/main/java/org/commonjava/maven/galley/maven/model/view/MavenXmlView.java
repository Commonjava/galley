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
package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenXmlView<T extends ProjectRef>
{

    private static final String EXPRESSION_PATTERN = ".*\\$\\{.+\\}.*";

    private static final String TEXT_SUFFIX = "/text()";

    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    protected final List<DocRef<T>> stack;

    //    protected final XPathManager xpath;

    protected StringSearchInterpolator ssi;

    protected final List<MavenXmlMixin<T>> mixins = new ArrayList<MavenXmlMixin<T>>();

    protected final Set<String> localOnlyPaths;

    protected final XMLInfrastructure xml;

    public MavenXmlView( final List<DocRef<T>> stack, final XPathManager xpath, final XMLInfrastructure xml, final String... localOnlyPaths )
    {
        this.stack = stack;
        //        this.xpath = xpath;
        this.xml = xml;
        this.localOnlyPaths = new HashSet<String>( Arrays.asList( localOnlyPaths ) );
    }

    public MavenXmlView( final List<DocRef<T>> stack, final XPathManager xpath, final XMLInfrastructure xml, final Set<String> localOnlyPaths )
    {
        this.stack = stack;
        //        this.xpath = xpath;
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
            value = resolveXPathExpression( "/project/properties/" + expression, true, -1 );
        }

        for ( int i = 0; value == null && activeProfileIds != null && i < activeProfileIds.length; i++ )
        {
            final String profileId = activeProfileIds[i];
            value = resolveXPathExpression( "//profile[id/text()=\"" + profileId + "\"]/properties/" + expression, true, -1, activeProfileIds );
        }

        return value;
    }

    public String resolveXPathExpression( final String path, final boolean cachePath, final int maxAncestry, final String... activeProfileIds )
    {
        final String p = trimTextSuffix( path );

        final String raw = resolveXPathToRawString( p, cachePath, maxAncestry );
        if ( raw != null )
        {
            //            logger.info( "Raw content of: '{}' is: '{}'", path, raw );
            return resolveExpressions( raw, activeProfileIds );
        }

        return null;
    }

    private String trimTextSuffix( final String path )
    {
        String p = path;
        if ( p.endsWith( TEXT_SUFFIX ) )
        {
            p = p.substring( 0, p.length() - TEXT_SUFFIX.length() );
        }

        return p;
    }

    public List<String> resolveXPathExpressionToAggregatedList( final String path, final boolean cachePath, final int maxAncestry )
    {
        final String p = trimTextSuffix( path );

        final List<Node> nodes = resolveXPathToAggregatedNodeList( p, cachePath, maxAncestry );
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

    public String resolveXPathToRawString( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenRuntimeException
    {
        String result = null;
        //        try
        //        {
        //            final XPathExpression expression = xpath.getXPath( path, cachePath );

        int maxAncestry = maxDepth;
        for ( final String pathPrefix : localOnlyPaths )
        {
            if ( path.startsWith( pathPrefix ) )
            {
                maxAncestry = 0;
                break;
            }
        }

        //        logger.info( "Resolving: {}", path );

        int ancestryDepth = 0;
        for ( final DocRef<T> dr : stack )
        {
            if ( maxAncestry > -1 && ancestryDepth > maxAncestry )
            {
                break;
            }

            result = (String) dr.getDocContext()
                                .getValue( path );
            //                result = (Node) expression.evaluate( dr.getDoc(), XPathConstants.NODE );
            //                logger.info( "Value of '{}' at depth: {} is: {}", path, ancestryDepth, result );

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
                                  .resolveXPathToRawString( path, true, maxAncestry );
                    //                        logger.info( "Value of '{}' in mixin: {} is: '{}'", path, mixin );
                }

                if ( result != null )
                {
                    break;
                }
            }
        }
        //        }
        //        catch ( final XPathExpressionException e )
        //        {
        //            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: {}. Reason: {}", e, path, e.getMessage() );
        //        }

        return result;
    }

    public Node resolveXPathToNode( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenRuntimeException
    {
        Node result = null;
        //        try
        //        {
        //            final XPathExpression expression = xpath.getXPath( path, cachePath );

        int maxAncestry = maxDepth;
        for ( final String pathPrefix : localOnlyPaths )
        {
            if ( path.startsWith( pathPrefix ) )
            {
                maxAncestry = 0;
                break;
            }
        }

        //        logger.info( "Resolving: {}", path );

        int ancestryDepth = 0;
        for ( final DocRef<T> dr : stack )
        {
            if ( maxAncestry > -1 && ancestryDepth > maxAncestry )
            {
                break;
            }

            result = (Node) dr.getDocContext()
                              .selectSingleNode( path );
            //                result = (Node) expression.evaluate( dr.getDoc(), XPathConstants.NODE );
            //                logger.info( "Value of '{}' at depth: {} is: {}", path, ancestryDepth, result );

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
                    //                        logger.info( "Value of '{}' in mixin: {} is: '{}'", path, mixin );
                }

                if ( result != null )
                {
                    break;
                }
            }
        }
        //        }
        //        catch ( final XPathExpressionException e )
        //        {
        //            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: {}. Reason: {}", e, path, e.getMessage() );
        //        }

        return result;
    }

    public synchronized List<Node> resolveXPathToAggregatedNodeList( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenRuntimeException
    {
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

            final List<Node> nodes = getLocalNodeList( dr.getDocContext(), path );
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

    public synchronized List<Node> resolveXPathToFirstNodeList( final String path, final boolean cachePath, final int maxDepth )
        throws GalleyMavenRuntimeException
    {
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

            final List<Node> result = getLocalNodeList( dr.getDocContext(), path );
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

    protected List<Node> getLocalNodeList( final JXPathContext context, final String path )
        throws GalleyMavenRuntimeException
    {
        final List<Node> result = new ArrayList<Node>();
        final List<?> iter = context.selectNodes( path );
        if ( iter != null )
        {
            for ( final Object obj : iter )
            {
                result.add( (Node) obj );
            }
        }

        return result;
    }

    protected String resolveXPathExpressionFrom( final JXPathContext context, final String path )
    {
        final String p = trimTextSuffix( path );

        final Node result = resolveXPathToNodeFrom( context, p, true );
        if ( result != null && result.getNodeType() == Node.TEXT_NODE )
        {
            return resolveExpressions( result.getTextContent()
                                             .trim() );
        }

        return null;
    }

    protected List<String> resolveXPathExpressionToListFrom( final JXPathContext context, final String path )
        throws GalleyMavenException
    {
        final String p = trimTextSuffix( path );

        final List<Node> nodes = resolveXPathToNodeListFrom( context, p, true );
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

    protected synchronized Node resolveXPathToNodeFrom( final JXPathContext context, final String path, final boolean cachePath )
    {
        return (Node) context.selectSingleNode( path );
    }

    public synchronized List<Node> resolveXPathToNodeListFrom( final JXPathContext context, final String path, final boolean cachePath )
        throws GalleyMavenRuntimeException
    {
        final List<Node> result = new ArrayList<Node>();
        final List<?> iter = context.selectNodes( path );
        if ( iter != null )
        {
            for ( final Object obj : iter )
            {
                result.add( (Node) obj );
            }
        }

        return result;
    }

    public boolean containsExpression( final String value )
    {
        return value != null && value.matches( EXPRESSION_PATTERN );
    }

    public String resolveExpressions( final String value, final String... activeProfileIds )
    {
        if ( !containsExpression( value ) )
        {
            //            logger.info( "No expressions in: '{}'", value );
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
            //            logger.info( "Resolved '{}' to '{}'", value, result );

            if ( result == null || result.trim()
                                         .length() < 1 )
            {
                result = value;
            }

            return result;
        }
        catch ( final InterpolationException e )
        {
            logger.error( String.format( "Failed to resolve expressions in: '%s'. Reason: %s", value, e.getMessage() ), e );
            return value;
        }
    }

    private static final class MavenPomViewVS<T extends ProjectRef>
        implements ValueSource
    {

        //        private final Logger logger = LoggerFactory.getLogger( getClass() );

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
                //                logger.info( "Value of: '{}' is: '{}'", expr, value );
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
