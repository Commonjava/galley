package org.commonjava.maven.galley.maven.model.view;

import static org.commonjava.maven.galley.maven.model.view.XPathManager.V;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.defaults.MavenPluginImplications;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
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
        final List<Node> depNodes =
            resolveXPathToAggregatedNodeList( "//dependency[not(ancestor::dependencyManagement) and not(ancestor::build) and not(ancestor::reporting)]",
                                              true, -1 );
        final List<DependencyView> depViews = new ArrayList<>( depNodes.size() );
        for ( final Node node : depNodes )
        {
            depViews.add( new DependencyView( this, (Element) node ) );
        }

        return depViews;
    }

    public List<DependencyView> getAllManagedDependencies()
        throws GalleyMavenException
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
        throws GalleyMavenException
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
        throws GalleyMavenException
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( path, true, -1 );
        final List<PluginView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( this, (Element) node, pluginDefaults, pluginImplications ) );
        }

        return result;
    }

    public List<DependencyView> getAllDependenciesMatching( final String path )
        throws GalleyMavenException
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
        throws GalleyMavenException
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( "/project//build/plugins/plugin", true, -1 );
        final List<PluginView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( this, (Element) node, pluginDefaults, pluginImplications ) );
        }

        return result;
    }

    public List<PluginView> getAllManagedBuildPlugins()
        throws GalleyMavenException
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( "/project//pluginManagement/plugins/plugin", true, -1 );
        final List<PluginView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new PluginView( this, (Element) node, pluginDefaults, pluginImplications ) );
        }

        return result;
    }

    public List<ProjectVersionRefView> getProjectVersionRefs( final String path )
        throws GalleyMavenException
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
        throws GalleyMavenException
    {
        final List<Node> list = resolveXPathToAggregatedNodeList( "/project//pluginManagement/plugins/plugin", true, -1 );
        final List<ProjectRefView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            final Node vNode = resolveXPathToNodeFrom( node, V, true );
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
