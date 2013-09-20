package org.commonjava.maven.galley.maven.view;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.defaults.MavenPluginDefaults;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenPomView
    extends AbstractMavenXmlView<ProjectVersionRef>
{

    private ProjectVersionRef versionedRef;

    private final MavenPluginDefaults pluginDefaults;

    public MavenPomView( final List<DocRef<ProjectVersionRef>> stack, final MavenPluginDefaults pluginDefaults )
    {
        super( stack );
        this.pluginDefaults = pluginDefaults;
    }

    public synchronized ProjectVersionRef asProjectVersionRef()
    {
        if ( versionedRef == null )
        {
            final String groupId = resolveXPathExpression( "/project/groupId/text()", false );
            final String artifactId = resolveXPathExpression( "/project/artifactId/text()", true );
            final String version = resolveXPathExpression( "/project/version/text()", false );

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

    public List<DependencyView> getAllDependencies()
    {
        final List<Node> depNodes = resolveXPathToNodeList( "//dependency", -1 );
        final List<DependencyView> depViews = new ArrayList<>( depNodes.size() );
        for ( final Node node : depNodes )
        {
            depViews.add( new DependencyView( this, (Element) node ) );
        }

        return depViews;
    }

    public List<DependencyView> getAllDirectDependencies()
    {
        final List<Node> depNodes = resolveXPathToNodeList( "//dependency[not(ancestor::dependencyManagement)]", -1 );
        final List<DependencyView> depViews = new ArrayList<>( depNodes.size() );
        for ( final Node node : depNodes )
        {
            depViews.add( new DependencyView( this, (Element) node ) );
        }

        return depViews;
    }

    public List<DependencyView> getAllManagedDependencies()
    {
        final List<Node> depNodes = resolveXPathToNodeList( "//dependencyManagement//dependency", -1 );
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
        return resolveXPathExpression( path, localOnly ? 0 : -1 );
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
        final List<Node> nodelist = resolveXPathToNodeList( path, localOnly ? 0 : -1 );
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
        return resolveXPathToNode( path, localOnly ? 0 : -1 );
    }

    public DependencyView asDependency( final Element depElement )
    {
        return new DependencyView( this, depElement );
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
        final List<Node> list = resolveXPathToNodeList( "/project//extension", -1 );
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
        final List<Node> list = resolveXPathToNodeList( path, -1 );
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

    public List<PluginView> getAllBuildPlugins()
    {
        final List<Node> list = resolveXPathToNodeList( "/project//build/plugins/plugin", -1 );
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
        final List<Node> list = resolveXPathToNodeList( "/project//pluginManagement/plugins/plugin", -1 );
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
        final List<Node> list = resolveXPathToNodeList( "/project//pluginManagement/plugins/plugin", -1 );
        final List<ProjectVersionRefView> result = new ArrayList<>( list.size() );
        for ( final Node node : list )
        {
            if ( node == null )
            {
                continue;
            }

            result.add( new ProjectVersionRefView( this, (Element) node ) );
        }

        return result;
    }

}
