package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.defaults.MavenPluginDefaults;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PluginView
    extends MavenGAVView
{

    private final MavenPluginDefaults pluginDefaults;

    private List<PluginDependencyView> pluginDependencies;

    protected PluginView( final MavenPomView pomView, final Element element, final MavenPluginDefaults pluginDefaults )
    {
        super( pomView, element, "build/pluginManagement/plugins/plugin" );
        this.pluginDefaults = pluginDefaults;
    }

    public boolean isManaged()
        throws GalleyMavenException
    {
        return pomView.resolveXPathToNodeFrom( element, "ancestor::pluginManagement", true ) != null;
    }

    public synchronized List<PluginDependencyView> getLocalPluginDependencies()
    {
        if ( pluginDependencies == null )
        {
            final List<Node> nodes = getFirstNodesWithManagement( "dependencies/dependency" );
            if ( nodes != null )
            {
                final List<PluginDependencyView> result = new ArrayList<>();
                for ( final Node node : nodes )
                {
                    result.add( new PluginDependencyView( pomView, (Element) node, this ) );
                }

                this.pluginDependencies = result;
            }
        }

        return pluginDependencies;
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

        // TODO: This isn't great (skipping match on groupId), but groupId can be implied...
        //        sb.append( G )
        //          .append( TEXTEQ )
        //          .append( getGroupId() )
        //          .append( QUOTE )
        //          .append( AND );

        sb.append( A )
          .append( TEXTEQ )
          .append( getArtifactId() )
          .append( QUOTE );

        return sb.toString();
    }
}
