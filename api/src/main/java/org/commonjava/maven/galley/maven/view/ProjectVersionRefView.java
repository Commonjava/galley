package org.commonjava.maven.galley.maven.view;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;

public class ProjectVersionRefView
    extends AbstractMavenGAVView
{

    protected ProjectVersionRefView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element, null );
    }

    protected ProjectVersionRefView( final MavenPomView pomView, final Element element, final String managementPath )
    {
        super( pomView, element, managementPath );
    }

    public boolean isVersioned()
    {
        return getVersion() != null;
    }

    @Override
    protected String getManagedViewQualifierFragment()
        throws GalleyMavenException
    {
        final StringBuilder sb = new StringBuilder();

        sb.append( G )
          .append( TEXTEQ )
          .append( getGroupId() )
          .append( "\" and " )
          .append( A )
          .append( TEXTEQ )
          .append( getArtifactId() )
          .append( "\"" );

        return sb.toString();
    }
}
