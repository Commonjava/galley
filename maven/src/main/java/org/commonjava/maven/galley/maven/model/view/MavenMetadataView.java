package org.commonjava.maven.galley.maven.model.view;

import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;

public class MavenMetadataView
    extends MavenXmlView<ProjectRef>
{

    public MavenMetadataView( final List<DocRef<ProjectRef>> stack, final XPathManager xpath )
    {
        super( stack, xpath );
    }

    public String resolveSingleValue( final String path )
        throws GalleyMavenException
    {
        return resolveXPathExpression( path, true, -1 );
    }

    public List<String> resolveValues( final String path )
        throws GalleyMavenException
    {
        return resolveXPathExpressionToAggregatedList( path, true, -1 );
    }

}
