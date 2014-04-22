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

import static org.commonjava.maven.galley.maven.model.view.XPathManager.V;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.maven.galley.maven.model.view.XPathManager.TLFunctionResolver;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenPomView
    extends MavenXmlView<ProjectVersionRef>
{

    private final ProjectVersionRef versionedRef;

    private final MavenPluginDefaults pluginDefaults;

    private final MavenPluginImplications pluginImplications;

    public MavenPomView( final ProjectVersionRef ref, final List<DocRef<ProjectVersionRef>> stack, final XPathManager xpath,
                         final MavenPluginDefaults pluginDefaults, final MavenPluginImplications pluginImplications, final XMLInfrastructure xml )
    {
        // define what xpaths are not inheritable...
        super( stack, xpath, xml, "/project/parent", "/project/artifactId" );
        this.pluginImplications = pluginImplications;

        if ( stack.isEmpty() )
        {
            throw new IllegalArgumentException( "Cannot create a POM view with no POMs!" );
        }

        this.versionedRef = ref;
        this.pluginDefaults = pluginDefaults;
    }

    @Override
    public String resolveMavenExpression( final String expression, final String... activeProfileIds )
        throws GalleyMavenException
    {
        String expr = expression;
        if ( expr.startsWith( "pom." ) )
        {
            expr = "project." + expr.substring( 4 );
        }

        return super.resolveMavenExpression( expr, activeProfileIds );
    }

    public synchronized ProjectVersionRef asProjectVersionRef()
    {
        return versionedRef;
    }

    public String getGroupId()
        throws GalleyMavenException
    {
        return asProjectVersionRef().getGroupId();
    }

    public String getArtifactId()
        throws GalleyMavenException
    {
        return asProjectVersionRef().getArtifactId();
    }

    public String getVersion()
        throws GalleyMavenException
    {
        return asProjectVersionRef().getVersionString();
    }

    public String getProfileIdFor( final Node node )
    {
        return resolveXPathExpressionFrom( node, "ancestor::profile/id/text()" );
    }

    public List<DependencyView> getAllDirectDependencies()
        throws GalleyMavenException
    {
        final List<MavenElementView> depNodes =
            resolveXPathToAggregatedElementViewList( "//dependency[not(ancestor::dependencyManagement) and not(ancestor::build) and not(ancestor::reporting)]",
                                                     true, -1 );
        final List<DependencyView> depViews = new ArrayList<DependencyView>( depNodes.size() );
        for ( final MavenElementView node : depNodes )
        {
            depViews.add( new DependencyView( node.getPomView(), node.getElement() ) );
        }

        return depViews;
    }

    public List<DependencyView> getAllManagedDependencies()
        throws GalleyMavenException
    {
        final List<MavenElementView> depNodes = resolveXPathToAggregatedElementViewList( "//dependencyManagement/dependencies/dependency", true, -1 );
        final List<DependencyView> depViews = new ArrayList<DependencyView>( depNodes.size() );
        for ( final MavenElementView node : depNodes )
        {
            depViews.add( new DependencyView( node.getPomView(), node.getElement() ) );
        }

        return depViews;
    }

    public List<DependencyView> getAllBOMs()
        throws GalleyMavenException
    {
        final List<MavenElementView> depNodes =
            resolveXPathToAggregatedElementViewList( "//dependencyManagement/dependencies/dependency[type/text()=\"pom\" and scope/text()=\"import\"]",
                                                     true, -1 );

        final List<DependencyView> depViews = new ArrayList<DependencyView>( depNodes.size() );
        for ( final MavenElementView node : depNodes )
        {
            depViews.add( new DependencyView( node.getPomView(), node.getElement() ) );
        }

        return depViews;
    }

    // TODO: Do these methods need to be here??

    public String resolveXPathExpression( final String path, final boolean localOnly )
        throws GalleyMavenException
    {
        final String value = resolveXPathExpression( path, true, localOnly ? 0 : -1 );
        return value;
    }

    public Element resolveXPathToElement( final String path, final boolean localOnly )
        throws GalleyMavenException
    {
        final Node node = resolveXPathToNode( path, true, localOnly ? 0 : -1 );
        if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
        {
            return (Element) node;
        }

        return null;
    }

    public List<MavenElementView> resolveXPathToElements( final String path, final boolean localOnly )
        throws GalleyMavenException
    {
        return resolveXPathToAggregatedElementViewList( path, true, localOnly ? 0 : -1 );
    }

    public synchronized Node resolveXPathToNode( final String path, final boolean localOnly )
        throws GalleyMavenException
    {
        final Node node = resolveXPathToNode( path, true, localOnly ? 0 : -1 );
        return node;
    }

    public MavenElementView resolveXPathToElementView( final String path, final boolean cachePath, final int maxDepth )
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
            Element n = null;
            for ( final DocRef<ProjectVersionRef> dr : stack )
            {
                if ( maxAncestry > -1 && ancestryDepth > maxAncestry )
                {
                    break;
                }

                final MavenPomView oldView = TLFunctionResolver.getPomView();
                try
                {
                    TLFunctionResolver.setPomView( this );

                    n = (Element) expression.evaluate( dr.getDoc(), XPathConstants.NODE );
                }
                finally
                {
                    TLFunctionResolver.setPomView( oldView );
                }
                //                logger.info( "Value of '{}' at depth: {} is: {}", path, ancestryDepth, result );

                if ( n != null )
                {
                    break;
                }

                ancestryDepth++;
            }

            if ( n != null )
            {
                return new MavenElementView( this, n );
            }

            MavenElementView result = null;
            for ( final MavenXmlMixin<ProjectVersionRef> mixin : mixins )
            {
                if ( mixin.matches( path ) )
                {
                    final MavenPomView mixinView = (MavenPomView) mixin.getMixin();
                    result = mixinView.resolveXPathToElementView( path, true, maxAncestry );
                    //                        logger.info( "Value of '{}' in mixin: {} is: '{}'", path, mixin );
                }

                if ( result != null )
                {
                    return result;
                }
            }
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: {}. Reason: {}", e, path, e.getMessage() );
        }

        return null;
    }

    public DependencyView asDependency( final Element depElement )
    {
        return new DependencyView( this, depElement );
    }

    public PluginView asPlugin( final Element element )
    {
        return new PluginView( this, element, pluginDefaults, pluginImplications );
    }

    public ParentView getParent()
        throws GalleyMavenException
    {
        final Element parentEl = (Element) resolveXPathToNode( "/project/parent", true );

        if ( parentEl != null )
        {
            return new ParentView( this, parentEl );
        }

        return null;
    }

    public List<ExtensionView> getBuildExtensions()
        throws GalleyMavenException
    {
        final List<MavenElementView> list = resolveXPathToAggregatedElementViewList( "/project//extension", true, -1 );
        final List<ExtensionView> result = new ArrayList<ExtensionView>( list.size() );
        for ( final MavenElementView node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new ExtensionView( node.getPomView(), node.getElement() ) );
        }

        return result;
    }

    public List<PluginView> getAllPluginsMatching( final String path )
        throws GalleyMavenException
    {
        final List<MavenElementView> list = resolveXPathToAggregatedElementViewList( path, true, -1 );
        final List<PluginView> result = new ArrayList<PluginView>( list.size() );
        for ( final MavenElementView node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( node.getPomView(), node.getElement(), pluginDefaults, pluginImplications ) );
        }

        return result;
    }

    public List<DependencyView> getAllDependenciesMatching( final String path )
        throws GalleyMavenException
    {
        final List<MavenElementView> list = resolveXPathToAggregatedElementViewList( path, true, -1 );
        final List<DependencyView> result = new ArrayList<DependencyView>( list.size() );
        for ( final MavenElementView node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new DependencyView( node.getPomView(), node.getElement() ) );
        }

        return result;
    }

    public List<PluginView> getAllBuildPlugins()
        throws GalleyMavenException
    {
        final List<MavenElementView> list = resolveXPathToAggregatedElementViewList( "/project//build/plugins/plugin", true, -1 );
        final List<PluginView> result = new ArrayList<PluginView>( list.size() );
        for ( final MavenElementView node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( node.getPomView(), node.getElement(), pluginDefaults, pluginImplications ) );
        }

        return result;
    }

    public List<PluginView> getAllManagedBuildPlugins()
        throws GalleyMavenException
    {
        final List<MavenElementView> list = resolveXPathToAggregatedElementViewList( "/project//pluginManagement/plugins/plugin", true, -1 );
        final List<PluginView> result = new ArrayList<PluginView>( list.size() );
        for ( final MavenElementView node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( node.getPomView(), node.getElement(), pluginDefaults, pluginImplications ) );
        }

        return result;
    }

    public List<ProjectVersionRefView> getProjectVersionRefs( final String path )
        throws GalleyMavenException
    {
        final List<MavenElementView> list = resolveXPathToAggregatedElementViewList( "/project//pluginManagement/plugins/plugin", true, -1 );
        final List<ProjectVersionRefView> result = new ArrayList<ProjectVersionRefView>( list.size() );
        for ( final MavenElementView node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new MavenGAVView( node.getPomView(), node.getElement() ) );
        }

        return result;
    }

    public List<ProjectRefView> getProjectRefs( final String path )
        throws GalleyMavenException
    {
        final List<MavenElementView> list = resolveXPathToAggregatedElementViewList( path, true, -1 );
        final List<ProjectRefView> result = new ArrayList<ProjectRefView>( list.size() );
        for ( final MavenElementView node : list )
        {
            if ( node == null )
            {
                continue;
            }

            final String v = node.getValue( V );
            if ( v != null )
            {
                result.add( new MavenGAVView( this, (Element) node ) );
            }
            else
            {
                result.add( new MavenGAView( this, (Element) node ) );
            }
        }

        return result;
    }

    public synchronized List<MavenElementView> resolveXPathToAggregatedElementViewList( final String path, final boolean cachePath, final int maxDepth )
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
            final List<MavenElementView> result = new ArrayList<MavenElementView>();
            for ( final DocRef<ProjectVersionRef> dr : stack )
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
                        result.add( new MavenElementView( this, (Element) node ) );
                    }
                }

                ancestryDepth++;
            }

            for ( final MavenXmlMixin<ProjectVersionRef> mixin : mixins )
            {
                if ( !mixin.matches( path ) )
                {
                    continue;
                }

                final MavenPomView mixinView = (MavenPomView) mixin.getMixin();
                final List<MavenElementView> nodes = mixinView.resolveXPathToAggregatedElementViewList( path, cachePath, maxAncestry );
                if ( nodes != null )
                {
                    for ( final MavenElementView node : nodes )
                    {
                        result.add( node );
                    }
                }
            }

            return result;
        }
        catch ( final XPathExpressionException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to retrieve content for xpath expression: {}. Reason: {}", e, path, e.getMessage() );
        }
    }

}
