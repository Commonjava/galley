package org.commonjava.maven.galley.maven.view;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenPomView
    extends AbstractMavenXmlView<ProjectVersionRef>
{

    public MavenPomView( final List<DocRef<ProjectVersionRef>> stack )
    {
        super( stack );
    }

    public ProjectVersionRef asProjectVersionRef()
    {
        final String groupId = resolveXPathExpression( "/project/groupId/text()", false );
        final String artifactId = resolveXPathExpression( "/project/artifactId/text()", true );
        final String version = resolveXPathExpression( "/project/version/text()", false );

        return new ProjectVersionRef( groupId, artifactId, version );
    }

    public String getProfileIdFor( final Node node )
        throws GalleyMavenException
    {
        return resolveXPathExpressionFrom( node, "ancestor::profile/id/text()" );
    }

    public List<DependencyView> getAllDependencies()
    {
        final List<Node> depNodes = resolveXPathToNodeList( "//dependencies/dependency", -1 );
        final List<DependencyView> depViews = new ArrayList<>( depNodes.size() );
        for ( final Node node : depNodes )
        {
            depViews.add( new DependencyView( this, (Element) node ) );
        }

        return depViews;
    }

    public List<DependencyView> getAllManagedDependencies()
    {
        final List<Node> depNodes = resolveXPathToNodeList( "//dependencyManagement/dependencies/dependency", -1 );
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

}
