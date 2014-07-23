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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;
import org.commonjava.maven.galley.maven.parse.JXPathUtils;
import org.commonjava.maven.galley.maven.parse.ResolveFunctions;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenPomView
    extends MavenXmlView<ProjectVersionRef>
{

    private static final String EXPRESSION_PATTERN = ".*\\$\\{.+\\}.*";

    private static final String TEXT_SUFFIX = "/text()";

    private final ProjectVersionRef versionedRef;

    private final MavenPluginDefaults pluginDefaults;

    private final MavenPluginImplications pluginImplications;

    private final Set<String> activeProfileIds;

    public MavenPomView( final ProjectVersionRef ref, final List<DocRef<ProjectVersionRef>> stack,
                         final XPathManager xpath, final MavenPluginDefaults pluginDefaults,
                         final MavenPluginImplications pluginImplications, final XMLInfrastructure xml,
                         final String... activeProfileIds )
    {
        // define what xpaths are not inheritable...
        super( stack, xpath, xml, "/project/parent", "/project/artifactId" );

        this.pluginImplications = pluginImplications;
        this.activeProfileIds = new HashSet<String>( Arrays.asList( activeProfileIds ) );

        if ( stack.isEmpty() )
        {
            throw new IllegalArgumentException( "Cannot create a POM view with no POMs!" );
        }

        this.versionedRef = ref;
        this.pluginDefaults = pluginDefaults;
    }

    public Set<String> getActiveProfileIds()
    {
        return activeProfileIds;
    }

    public String resolveMavenExpression( final String expression, final String... activeProfileIds )
        throws GalleyMavenException
    {
        String expr = expression.replace( '.', '/' );
        if ( expr.startsWith( "pom" ) )
        {
            expr = "/project/" + expr.substring( 4 );
        }

        if ( !expr.startsWith( "/" ) )
        {
            expr = "/" + expr;
        }

        if ( !expr.startsWith( "/project" ) )
        {
            expr = "/project" + expr;
        }

        String value = resolveXPathExpression( expr, true, -1 );

        for ( int i = 0; value == null && activeProfileIds != null && i < activeProfileIds.length; i++ )
        {
            final String profileId = activeProfileIds[i];
            value =
                resolveXPathExpression( "//profile[id/text()=\"" + profileId + "\"]/properties/" + expression, true,
                                        -1, activeProfileIds );
        }

        if ( value == null )
        {
            final List<Node> propertyNodes = resolveXPathToAggregatedNodeList( "/project/properties/*", true, -1 );
            for ( final Node propertyNode : propertyNodes )
            {
                if ( expression.equals( propertyNode.getNodeName() ) )
                {
                    value = propertyNode.getTextContent()
                                        .trim();
                    break;
                }
            }
        }

        return value;
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

    public String getProfileIdFor( final Element element )
    {
        Node parent = element;
        do
        {
            parent = parent.getParentNode();
        }
        while ( parent != null && !"profile".equals( parent.getNodeName() ) );

        if ( parent == null )
        {
            return null;
        }

        return (String) JXPathUtils.newContext( parent )
                                   .getValue( "id" );
    }

    public List<DependencyView> getAllDirectDependencies()
        throws GalleyMavenException
    {
        final String xp =
            "//dependency[not(ancestor::dependencyManagement) and not(ancestor::build) and not(ancestor::reporting)]";
        //        final String xp = "./project/dependencies/dependency";
        final List<MavenElementView> depNodes = resolveXPathToAggregatedElementViewList( xp, true, -1 );
        final List<DependencyView> depViews = new ArrayList<DependencyView>( depNodes.size() );
        for ( final MavenElementView node : depNodes )
        {
            depViews.add( new DependencyView( node.getPomView(), node.getElement() ) );
        }

        return depViews;
    }

    /**
     * Reads all managed dependencies from this pom. It means all from pom 
     * itself, from its parents and also from imported BOM poms.
     * 
     * @return list of read dependencies
     */
    public List<DependencyView> getAllManagedDependencies()
        throws GalleyMavenException
    {
        final List<MavenElementView> depNodes =
            resolveXPathToAggregatedElementViewList( "//dependencyManagement/dependencies/dependency[not(scope/text()=\"import\")]",
                                                     true, -1 );
        final List<DependencyView> depViews = new ArrayList<DependencyView>( depNodes.size() );
        for ( final MavenElementView node : depNodes )
        {
            depViews.add( new DependencyView( node.getPomView(), node.getElement() ) );
        }

        return depViews;
    }

    /**
     * Reads managed dependencies specified in this pom itself and its parents. 
     * It omits managed dependencies specified in imported BOM poms.
     * 
     * @return list of read dependencies
     */
    public List<DependencyView> getManagedDependenciesNoImports()
    {
        final List<MavenElementView> depNodes =
            resolveXPathToAggregatedElementViewList( "//dependencyManagement/dependencies/dependency[not(scope/text()=\"import\")]",
                                                     true, -1, false );
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

            final MavenPomView oldView = ResolveFunctions.getPomView();
            try
            {
                ResolveFunctions.setPomView( this );
                n = (Element) dr.getDocContext()
                                .selectSingleNode( path );
            }
            finally
            {
                ResolveFunctions.setPomView( oldView );
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
        final List<MavenElementView> list =
            resolveXPathToAggregatedElementViewList( "/project//extensions/extension", true, -1 );
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
        final List<MavenElementView> list =
            resolveXPathToAggregatedElementViewList( "/project//build/plugins/plugin", true, -1 );
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
        final List<MavenElementView> list =
            resolveXPathToAggregatedElementViewList( "/project//pluginManagement/plugins/plugin", true, -1 );
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
        final List<MavenElementView> list =
            resolveXPathToAggregatedElementViewList( "/project//pluginManagement/plugins/plugin", true, -1 );
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

    public List<MavenElementView> resolveXPathToAggregatedElementViewList( final String path,
                                                                           final boolean cachePath,
                                                                           final int maxDepth )
    {
        return resolveXPathToAggregatedElementViewList( path, cachePath, maxDepth, true );
    }

    public synchronized List<MavenElementView> resolveXPathToAggregatedElementViewList( final String path,
                                                                                        final boolean cachePath,
                                                                                        final int maxDepth, 
                                                                                        final boolean includeMixins )
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
        final List<MavenElementView> result = new ArrayList<MavenElementView>();
        for ( final DocRef<ProjectVersionRef> dr : stack )
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
                    result.add( new MavenElementView( this, (Element) node ) );
                }
            }

            ancestryDepth++;
        }

        if ( includeMixins )
        {
            for ( final MavenXmlMixin<ProjectVersionRef> mixin : mixins )
            {
                if ( !mixin.matches( path ) )
                {
                    continue;
                }
                
                final MavenPomView mixinView = (MavenPomView) mixin.getMixin();
                final List<MavenElementView> nodes =
                        mixinView.resolveXPathToAggregatedElementViewList( path, cachePath, maxAncestry, includeMixins );
                if ( nodes != null )
                {
                    for ( final MavenElementView node : nodes )
                    {
                        result.add( node );
                    }
                }
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

    public String resolveXPathExpression( final String path, final boolean cachePath, final int maxAncestry,
                                          final String... activeProfileIds )
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

    public List<String> resolveXPathExpressionToAggregatedList( final String path, final boolean cachePath,
                                                                final int maxAncestry )
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
                ssi.addValueSource( new MavenPomViewVS( this, activeProfileIds ) );
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
            logger.error( String.format( "Failed to resolve expressions in: '%s'. Reason: %s", value, e.getMessage() ),
                          e );
            return value;
        }
    }

    private static final class MavenPomViewVS
        implements ValueSource
    {

        //        private final Logger logger = LoggerFactory.getLogger( getClass() );

        private final MavenPomView view;

        private final List<Object> feedback = new ArrayList<Object>();

        private final String[] activeProfileIds;

        public MavenPomViewVS( final MavenPomView view, final String[] activeProfileIds )
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

}
