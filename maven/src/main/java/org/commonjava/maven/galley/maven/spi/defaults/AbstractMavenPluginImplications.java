/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.maven.spi.defaults;

import static org.commonjava.maven.galley.maven.model.view.XPathManager.A;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.G;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.V;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.PluginDependencyView;
import org.commonjava.maven.galley.maven.model.view.PluginView;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.w3c.dom.Element;

public abstract class AbstractMavenPluginImplications
    implements MavenPluginImplications
{

    protected final XMLInfrastructure xml;

    protected AbstractMavenPluginImplications( final XMLInfrastructure xml )
    {
        this.xml = xml;
    }

    protected PluginDependencyView createPluginDependency( final PluginView pv, final ProjectRef ref )
        throws GalleyMavenException
    {
        final Map<String, String> map = new LinkedHashMap<String, String>();
        map.put( G, ref.getGroupId() );
        map.put( A, ref.getArtifactId() );
        map.put( V, pv.getVersion() );

        final Element element = pv.getElement();
        return new PluginDependencyView( pv.getPomView(), pv, xml.createElement( element, "dependencies/dependency", map ) );
    }

    @Override
    public Set<PluginDependencyView> getImpliedPluginDependencies( final PluginView pv )
        throws GalleyMavenException
    {
        final Map<ProjectRef, Set<ProjectRef>> impliedDepMap = getImpliedRefMap();
        final ProjectRef ref = pv.asProjectRef();
        final Set<ProjectRef> implied = impliedDepMap.get( ref );

        if ( implied == null || implied.isEmpty() )
        {
            return null;
        }

        final Set<PluginDependencyView> views = new HashSet<PluginDependencyView>( implied.size() );
        for ( final ProjectRef impliedRef : implied )
        {
            final PluginDependencyView pd = createPluginDependency( pv, impliedRef );
            views.add( pd );
        }

        return views;
    }

    protected abstract Map<ProjectRef, Set<ProjectRef>> getImpliedRefMap();

}
