package org.commonjava.maven.galley.maven.model.view;

import org.commonjava.maven.atlas.ident.DependencyScope;
import org.w3c.dom.Element;

public class PluginDependencyView
    extends DependencyView
{

    private final PluginView plugin;

    public PluginDependencyView( final MavenPomView pomView, final Element element, final PluginView plugin )
    {
        super( pomView, element );
        this.plugin = plugin;
    }

    public PluginView getPlugin()
    {
        return plugin;
    }

    @Override
    public synchronized DependencyScope getScope()
    {
        return DependencyScope.toolchain;
    }

}
