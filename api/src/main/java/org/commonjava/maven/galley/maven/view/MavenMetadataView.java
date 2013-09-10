package org.commonjava.maven.galley.maven.view;

import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;

public class MavenMetadataView
    extends AbstractMavenXmlView<ProjectRef>
{

    public MavenMetadataView( final List<DocRef<ProjectRef>> stack )
    {
        super( stack );
    }

    public String resolveSingleValue( final String path )
        throws GalleyMavenException
    {
        return resolveXPathExpression( path, -1 );
    }

    public List<String> resolveValues( final String path )
        throws GalleyMavenException
    {
        return resolveXPathExpressionToList( path, -1 );
    }

}
