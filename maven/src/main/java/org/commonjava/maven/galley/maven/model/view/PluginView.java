package org.commonjava.maven.galley.maven.model.view;

import static org.commonjava.maven.galley.maven.model.view.XPathManager.A;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.QUOTE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.TEXTEQ;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.defaults.MavenPluginImplications;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PluginView
    extends MavenGAVView
{

    private final MavenPluginDefaults pluginDefaults;

    private List<PluginDependencyView> pluginDependencies;

    private final MavenPluginImplications pluginImplications;

    protected PluginView( final MavenPomView pomView, final Element element, final MavenPluginDefaults pluginDefaults,
                          final MavenPluginImplications pluginImplications )
    {
        super( pomView, element, "build/pluginManagement/plugins/plugin" );
        this.pluginDefaults = pluginDefaults;
        this.pluginImplications = pluginImplications;
    }

    public boolean isManaged()
        throws GalleyMavenException
    {
        return pomView.resolveXPathToNodeFrom( element, "ancestor::pluginManagement", true ) != null;
    }

    public synchronized List<PluginDependencyView> getLocalPluginDependencies()
        throws GalleyMavenException
    {
        if ( pluginDependencies == null )
        {
            final List<PluginDependencyView> result = new ArrayList<>();

            final List<Node> nodes = getFirstNodesWithManagement( "dependencies/dependency" );
            if ( nodes != null )
            {
                for ( final Node node : nodes )
                {
                    result.add( new PluginDependencyView( pomView, this, (Element) node ) );
                }

                this.pluginDependencies = result;
            }

            final Set<PluginDependencyView> implied = pluginImplications.getImpliedPluginDependencies( this );
            if ( implied != null && !implied.isEmpty() )
            {
                for ( final PluginDependencyView impliedDep : implied )
                {
                    if ( !result.contains( impliedDep ) )
                    {
                        result.add( impliedDep );
                    }
                }
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
