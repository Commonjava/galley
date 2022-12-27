/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.maven.model.view;

import static org.commonjava.maven.galley.maven.model.view.XPathManager.A;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.AND;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.END_PAREN;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.EQQUOTE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.G;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.QUOTE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.RESOLVE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.TEXT;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class PluginView
    extends MavenGAVView
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final MavenPluginDefaults pluginDefaults;

    private List<PluginDependencyView> pluginDependencies;

    private final MavenPluginImplications pluginImplications;

    protected PluginView( final MavenPomView pomView, final Element element, final OriginInfo originInfo, final MavenPluginDefaults pluginDefaults,
                          final MavenPluginImplications pluginImplications )
    {
        super( pomView, element, originInfo, "build/pluginManagement/plugins/plugin" );
        this.pluginDefaults = pluginDefaults;
        this.pluginImplications = pluginImplications;
    }

    public boolean isManaged()
    {
        return xmlView.resolveXPathToNodeFrom( elementContext, "ancestor::pluginManagement", true ) != null;
    }

    public synchronized List<PluginDependencyView> getLocalPluginDependencies()
        throws GalleyMavenException
    {
        if ( pluginDependencies == null )
        {
            final List<PluginDependencyView> result = new ArrayList<>();

            final List<XmlNodeInfo> nodes = getFirstNodesWithManagement( "dependencies/dependency" );
            if ( nodes != null )
            {
                for ( final XmlNodeInfo node : nodes )
                {
                    logger.debug( "Adding plugin dependency for: {}", node.getNode().getNodeName() );
                    result.add( new PluginDependencyView( xmlView, this, (Element) node.getNode(), node.getOriginInfo() ) );
                }

                this.pluginDependencies = result;
            }
        }

        return pluginDependencies;
    }

    public Set<PluginDependencyView> getImpliedPluginDependencies()
        throws GalleyMavenException
    {
        return pluginImplications.getImpliedPluginDependencies( this );
    }

    @Override
    public synchronized String getVersion()
        throws GalleyMavenException
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

        final String aid = getArtifactId();
        final String gid = getGroupId();
        final String dgid = pluginDefaults.getDefaultGroupId( aid );
        if ( !gid.equals( dgid ) )
        {
            sb.append( RESOLVE )
              .append( G )
              .append( TEXT )
              .append( END_PAREN )
              .append( EQQUOTE )
              .append( gid )
              .append( QUOTE )
              .append( AND );
        }

        sb.append( RESOLVE )
          .append( A )
          .append( TEXT )
          .append( END_PAREN )
          .append( EQQUOTE )
          .append( aid )
          .append( QUOTE );

        return sb.toString();
    }

}
