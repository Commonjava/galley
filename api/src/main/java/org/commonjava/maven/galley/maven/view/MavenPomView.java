package org.commonjava.maven.galley.maven.view;

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

    public synchronized Node resolveXPathToNode( final String path, final boolean localOnly )
        throws GalleyMavenException
    {
        return resolveXPathToNode( path, localOnly ? 0 : -1 );
    }

}
