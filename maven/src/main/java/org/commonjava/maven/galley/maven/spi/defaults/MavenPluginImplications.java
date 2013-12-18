package org.commonjava.maven.galley.maven.spi.defaults;

import java.util.Set;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.PluginDependencyView;
import org.commonjava.maven.galley.maven.model.view.PluginView;

public interface MavenPluginImplications
{

    Set<PluginDependencyView> getImpliedPluginDependencies( PluginView pv )
        throws GalleyMavenException;

}
