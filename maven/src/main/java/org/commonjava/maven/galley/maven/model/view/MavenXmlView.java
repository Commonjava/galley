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
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenXmlView<T extends ProjectRef>
{

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

            try
            {
                result = (String) dr.getDocContext()
                                .getValue( path );
            }
            catch ( final JXPathInvalidSyntaxException e )
            {
                logger.debug( "[ABORT XPath] Error resolving '{}' from '{}': {}", e, path, dr.getSource(),
                              e.getMessage() );
                return null;
            }
            catch ( final JXPathException e )
            {
                logger.debug( "[SKIP XPath-Doc] Error resolving '{}' from '{}': {}", e, path, dr.getSource(),
                             e.getMessage() );
                continue;
            }

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

    public List<String> resolveXPathToAggregatedStringList( final String path, final boolean cachePath,
                                                            final int maxAncestry )
    {
        final List<Node> nodes = resolveXPathToAggregatedNodeList( path, cachePath, maxAncestry );
        final List<String> result = new ArrayList<String>( nodes.size() );
        for ( final Node node : nodes )
        {
            if ( node != null && node.getNodeType() == Node.TEXT_NODE )
            {
                result.add( node.getTextContent()
                                .trim() );
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
