package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.defaults.MavenPluginDefaults;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenPomView
    extends MavenXmlView<ProjectVersionRef>
{

    private ProjectVersionRef versionedRef;

    private final MavenPluginDefaults pluginDefaults;

    public MavenPomView( final ProjectVersionRef ref, final List<DocRef<ProjectVersionRef>> stack, final XPathManager xpath,
                         final MavenPluginDefaults pluginDefaults )
    {
        // define what xpaths are not inheritable...
        super( stack, xpath, "/project/parent", "/project/artifactId" );

        if ( stack.isEmpty() )
        {
            throw new IllegalArgumentException( "Cannot create a POM view with no POMs!" );
        }

        this.versionedRef = ref;
        this.pluginDefaults = pluginDefaults;
    }

    public MavenPomView( final List<DocRef<ProjectVersionRef>> stack, final XPathManager xpath, final MavenPluginDefaults pluginDefaults )
    {
        this( null, stack, xpath, pluginDefaults );
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
        if ( versionedRef == null )
        {
            final String groupId = resolveXPathExpression( "/project/groupId/text()", true, -1 );
            final String artifactId = resolveXPathExpression( "/project/artifactId/text()", true, 0 );
            final String version = resolveXPathExpression( "/project/version/text()", true, -1 );

            versionedRef = new ProjectVersionRef( groupId, artifactId, version );
        }

        return versionedRef;
    }

    public String getGroupId()
    {
        return asProjectVersionRef().getGroupId();
    }

    public String getArtifactId()
    {
        return asProjectVersionRef().getArtifactId();
    }

    public String getVersion()
    {
        return asProjectVersionRef().getVersionString();
    }

    public String getProfileIdFor( final Node node )
        throws GalleyMavenException
    {
        return resolveXPathExpressionFrom( node, "ancestor::profile/id/text()" );
    }

    public List<DependencyView> getAllDirectDependencies()
    {
        final List<Node> depNodes = resolveXPathToAggregatedNodeList( "//dependency[not(ancestor::dependencyManagement)]", true, -1 );
        final List<DependencyView> depViews = new ArrayList<>( depNodes.size() );
        for ( final Node node : depNodes )
        {
            depViews.add( new DependencyView( this, (Element) node ) );
        }

        return depViews;
    }

    public List<DependencyView> getAllManagedDependencies()
    {
        final List<Node> depNodes = resolveXPathToAggregatedNodeList( "//dependencyManagement/dependencies/dependency", true, -1 );
        final List<DependencyView> depViews = new ArrayList<>( depNodes.size() );
        for ( final Node node : depNodes )
        {
            depViews.add( new DependencyView( this, (Element) node ) );
        }

        return depViews;
    }

    public List<DependencyView> getAllBOMs()
    {
        final List<Node> depNodes =
            resolveXPathToAggregatedNodeList( "//dependencyManagement/dependencies/dependency[type/text()=\"pom\" and scope/text()=\"import\"]",
                                              true, -1 );

        final List<DependencyView> depViews = new ArrayList<>( depNodes.size() );
        for ( final Node node : depNodes )
        {
            depViews.add( new DependencyView( this, (Element) node ) );
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
        final Node node = resolveXPathToNode( path, localOnly );
        if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
        {
            return (Element) node;
        }

        return null;
    }

    public List<Element> resolveXPathToElements( final String path, final boolean localOnly )
        throws GalleyMavenException
    {
        final List<Node> nodelist = resolveXPathToAggregatedNodeList( path, true, localOnly ? 0 : -1 );
        final List<Element> result = new ArrayList<>( nodelist.size() );
        for ( final Node node : nodelist )
        {
            if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
            {
                result.add( (Element) node );
            }
        }

        return result;
    }

    public synchronized Node resolveXPathToNode( final String path, final boolean localOnly )
        throws GalleyMavenException
    {
        final Node node = resolveXPathToNode( path, true, localOnly ? 0 : -1 );
        return node;
    }

    public DependencyView asDependency( final Element depElement )
    {
        return new DependencyView( this, depElement );
    }

    public PluginView asPlugin( final Element element )
    {
        return new PluginView( this, element, pluginDefaults );
    }

    public ParentView getParent()
    {
        final Element parentEl = (Element) resolveXPathToNode( "/project/parent", true );

        if ( parentEl != null )
        {
            return new ParentView( this, parentEl );
        }

        return null;
    }

    public List<ExtensionView> getBuildExtensions()
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( "/project//extension", true, -1 );
        final List<ExtensionView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new ExtensionView( this, (Element) node ) );
        }

        return result;
    }

    public List<PluginView> getAllPluginsMatching( final String path )
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( path, true, -1 );
        final List<PluginView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( this, (Element) node, pluginDefaults ) );
        }

        return result;
    }

    public List<DependencyView> getAllDependenciesMatching( final String path )
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( path, true, -1 );
        final List<DependencyView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new DependencyView( this, (Element) node ) );
        }

        return result;
    }

    public List<PluginView> getAllBuildPlugins()
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( "/project//build/plugins/plugin", true, -1 );
        final List<PluginView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( this, (Element) node, pluginDefaults ) );
        }

        return result;
    }

    public List<PluginView> getAllManagedBuildPlugins()
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( "/project//pluginManagement/plugins/plugin", true, -1 );
        final List<PluginView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( this, (Element) node, pluginDefaults ) );
        }

        return result;
    }

    public List<ProjectVersionRefView> getProjectVersionRefs( final String path )
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( "/project//pluginManagement/plugins/plugin", true, -1 );
        final List<ProjectVersionRefView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new MavenGAVView( this, (Element) node ) );
        }

        return result;
    }

    public List<ProjectRefView> getProjectRefs( final String path )
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( "/project//pluginManagement/plugins/plugin", true, -1 );
        final List<ProjectRefView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            final Node vNode = resolveXPathToNodeFrom( node, MavenElementView.V, true );
            if ( vNode != null )
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

}
