package org.commonjava.maven.galley.maven.model.view;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.defaults.MavenPluginDefaults;
import org.w3c.dom.Element;

public class PluginView
    extends MavenGAVView
{

    private final MavenPluginDefaults pluginDefaults;

    protected PluginView( final MavenPomView pomView, final Element element, final MavenPluginDefaults pluginDefaults )
    {
        super( pomView, element, "pluginManagement/plugins/plugin" );
        this.pluginDefaults = pluginDefaults;
    }

    public boolean isManaged()
        throws GalleyMavenException
    {
        return pomView.resolveXPathToNodeFrom( element, "ancestor::pluginManagement", true ) != null;
    }

    @Override
    public synchronized String getVersion()
    {
        if ( super.getVersion() == null )
        {
            setVersion( pluginDefaults.getDefaultVersion( getGroupId(), getArtifactId() ) );
        }

        return super.getVersion();
    }

    @Override
    public synchronized String getGroupId()
    {
        final String gid = super.getGroupId();
        if ( gid == null )
        {
            setGroupId( pluginDefaults.getDefaultGroupId( getArtifactId() ) );
        }

        return super.getGroupId();
    }

    @Override
    protected String getManagedViewQualifierFragment()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append( G )
          .append( TEXTEQ )
          .append( getGroupId() )
          .append( QUOTE )
          .append( AND )
          .append( A )
          .append( TEXTEQ )
          .append( getArtifactId() )
          .append( QUOTE );

        return sb.toString();
    }
}
