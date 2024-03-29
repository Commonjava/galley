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
package org.commonjava.maven.galley.maven.rel;

import org.commonjava.atlas.maven.graph.model.EProjectDirectRelationships;
import org.commonjava.atlas.maven.graph.model.EProjectDirectRelationships.Builder;
import org.commonjava.atlas.maven.graph.rel.SimpleBomRelationship;
import org.commonjava.atlas.maven.graph.rel.SimpleDependencyRelationship;
import org.commonjava.atlas.maven.graph.rel.SimpleExtensionRelationship;
import org.commonjava.atlas.maven.graph.rel.SimpleParentRelationship;
import org.commonjava.atlas.maven.graph.rel.SimplePluginDependencyRelationship;
import org.commonjava.atlas.maven.graph.rel.SimplePluginRelationship;
import org.commonjava.atlas.maven.graph.util.RelationshipUtils;
import org.commonjava.atlas.maven.ident.ref.ArtifactRef;
import org.commonjava.atlas.maven.ident.ref.InvalidRefException;
import org.commonjava.atlas.maven.ident.ref.ProjectRef;
import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.atlas.maven.ident.version.InvalidVersionSpecificationException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DependencyView;
import org.commonjava.maven.galley.maven.model.view.ExtensionView;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.model.view.ParentView;
import org.commonjava.maven.galley.maven.model.view.PluginDependencyView;
import org.commonjava.maven.galley.maven.model.view.PluginView;
import org.commonjava.maven.galley.maven.model.view.ProjectRefView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MavenModelProcessor
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public EProjectDirectRelationships readRelationships( final MavenPomView pomView, final URI source,
                                              final ModelProcessorConfig discoveryConfig )
        throws GalleyMavenException
    {
        final boolean includeManagedDependencies = discoveryConfig.isIncludeManagedDependencies();
        final boolean includeBuildSection = discoveryConfig.isIncludeBuildSection();
        final boolean includeManagedPlugins = discoveryConfig.isIncludeManagedPlugins();

        logger.info( "Reading relationships for: {}\n  (from: {})", pomView.getRef(), source );

        try
        {
            final ProjectVersionRef projectRef = pomView.getRef();

            final EProjectDirectRelationships.Builder builder =
                new EProjectDirectRelationships.Builder( source, projectRef );

            addParentRelationship( source, builder, pomView, projectRef );

            addDependencyRelationships( source, builder, pomView, projectRef, includeManagedDependencies );

            if ( includeBuildSection )
            {
                addExtensionUsages( source, builder, pomView, projectRef );
                addPluginUsages( source, builder, pomView, projectRef, includeManagedPlugins );
            }

            return builder.build();
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            throw new GalleyMavenException( "Failed to parse version for model: {}. Reason: {}", e, pomView,
                                   e.getMessage() );
        }
        catch ( final IllegalArgumentException e )
        {
            throw new GalleyMavenException( "Failed to parse relationships for model: {}. Reason: {}", e, pomView,
                                          e.getMessage() );
        }
    }

    private void addExtensionUsages( final URI source, final Builder builder, final MavenPomView pomView,
                                     final ProjectVersionRef projectRef )
    {
        List<ExtensionView> extensions = null;
        try
        {
            extensions = pomView.getBuildExtensions();
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            logger.error( String.format( "%s: Cannot retrieve build extensions: %s", pomView.getRef(), e.getMessage() ),
                          e );
        }
        catch ( final InvalidRefException e )
        {
            logger.error( String.format( "%s: Cannot retrieve build extensions: %s", pomView.getRef(), e.getMessage() ),
                          e );
        }

        if(extensions!=null)
        {
            for ( final ExtensionView ext : extensions )
            {
                if ( ext == null )
                {
                    continue;
                }

                try
                {
                    final ProjectVersionRef ref = ext.asProjectVersionRef();

                    // force the InvalidVersionSpecificationException.
                    ref.getVersionSpec();

                    builder.withExtensions( new SimpleExtensionRelationship( source, projectRef, ref, builder.getNextExtensionIndex(),
                                                                             ext.getOriginInfo().isInherited() ) );
                }
                catch ( final InvalidRefException | InvalidVersionSpecificationException | GalleyMavenException e )
                {
                    logger.error( String.format( "%s: Build extension is invalid! Reason: %s. Skipping:\n\n%s\n\n",
                                                 pomView.getRef(), e.getMessage(), ext.toXML() ), e );
                }
            }
        }
    }

    private void addPluginUsages( final URI source, final Builder builder, final MavenPomView pomView,
                                  final ProjectVersionRef projectRef, final boolean includeManagedPlugins )
    {
        addBuildPluginUsages( source, builder, pomView, projectRef, includeManagedPlugins );
        addReportPluginUsages( source, builder, pomView, projectRef );
        addSiteReportPluginUsages( source, builder, pomView, projectRef );
    }

    private void addSiteReportPluginUsages( final URI source, final Builder builder, final MavenPomView pomView,
                                            final ProjectVersionRef projectRef )
    {
        //        final List<ProjectVersionRefView> refs = pomView.getProjectVersionRefs( "//plugin[artifactId/text()=\"maven-site-plugin\"]//reportPlugin" );

        List<PluginView> plugins = null;
        try
        {
            plugins = pomView.getAllPluginsMatching( "//plugin[artifactId/text()=\"maven-site-plugin\"]//reportPlugin" );
        }
        catch ( final InvalidRefException e )
        {
            logger.error( String.format( "%s: Cannot retrieve site-plugin nested reporting plugins: %s",
                                         pomView.getRef(), e.getMessage() ), e );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            logger.error( String.format( "%s: Cannot retrieve site-plugin nested reporting plugins: %s",
                                         pomView.getRef(), e.getMessage() ), e );
        }

        addPlugins( plugins, projectRef, builder, source, false );
    }

    public void addReportPluginUsages( final URI source, final Builder builder, final MavenPomView pomView,
                                       final ProjectVersionRef projectRef )
    {
        List<PluginView> plugins = null;
        try
        {
            plugins = pomView.getAllPluginsMatching( "//reporting/plugins/plugin" );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            logger.error( String.format( "%s: Cannot retrieve reporting plugins: %s", pomView.getRef(), e.getMessage() ),
                          e );
        }
        catch ( final InvalidRefException e )
        {
            logger.error( String.format( "%s: Cannot retrieve reporting plugins: %s", pomView.getRef(), e.getMessage() ),
                          e );
        }

        addPlugins( plugins, projectRef, builder, source, false );
    }

    public void addBuildPluginUsages( final URI source, final Builder builder, final MavenPomView pomView,
                                      final ProjectVersionRef projectRef, final boolean includeManagedPlugins )
    {
        if ( includeManagedPlugins )
        {
            List<PluginView> plugins = null;
            try
            {
                plugins = pomView.getAllManagedBuildPlugins();
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                logger.error( String.format( "%s: Cannot retrieve managed plugins: %s", pomView.getRef(),
                                             e.getMessage() ), e );
            }
            catch ( final InvalidRefException e )
            {
                logger.error( String.format( "%s: Cannot retrieve managed plugins: %s", pomView.getRef(),
                                             e.getMessage() ), e );
            }

            addPlugins( plugins, projectRef, builder, source, true );
        }

        List<PluginView> plugins = null;
        try
        {
            plugins = pomView.getAllBuildPlugins();
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            logger.error( String.format( "%s: Cannot retrieve build plugins: %s", pomView.getRef(), e.getMessage() ), e );
        }
        catch ( final InvalidRefException e )
        {
            logger.error( String.format( "%s: Cannot retrieve build plugins: %s", pomView.getRef(), e.getMessage() ), e );
        }

        addPlugins( plugins, projectRef, builder, source, false );
    }

    private void addPlugins( final List<PluginView> plugins, final ProjectVersionRef projectRef, final Builder builder,
                             final URI source, final boolean managed )
    {
        if ( plugins != null )
        {
            for ( final PluginView plugin : plugins )
            {
                ProjectVersionRef pluginRef;
                try
                {
                    if ( plugin.getVersion() == null )
                    {
                        logger.error( "{}: Cannot find a version for plugin: {}. Skipping.", projectRef, plugin.toXML() );
                        continue;
                    }

                    pluginRef = plugin.asProjectVersionRef();

                    // force the InvalidVersionSpecificationException.
                    pluginRef.getVersionSpec();

                    final String profileId = plugin.getProfileId();
                    final URI location = RelationshipUtils.profileLocation( profileId );

                    boolean inherited = plugin.getOriginInfo().isInherited();
                    boolean mixin = plugin.getOriginInfo().isMixin();
                    builder.withPlugins( new SimplePluginRelationship( source, location, projectRef, pluginRef,
                                                                 builder.getNextPluginDependencyIndex( projectRef,
                                                                                                       managed,
                                                                                                       inherited ),
                                                                 managed,
                                                                 inherited ) );
                }
                catch ( final GalleyMavenException e )
                {
                    logger.error( String.format( "%s: plugin is invalid! Reason: %s. Skipping:\n\n%s\n\n", projectRef,
                                                 e.getMessage(), plugin.toXML() ), e );
                    continue;
                }
                catch ( final InvalidVersionSpecificationException e )
                {
                    logger.error( String.format( "%s: plugin is invalid! Reason: %s. Skipping:\n\n%s\n\n", projectRef,
                                                 e.getMessage(), plugin.toXML() ), e );
                    continue;
                }
                catch ( final InvalidRefException e )
                {
                    logger.error( String.format( "%s: plugin is invalid! Reason: %s. Skipping:\n\n%s\n\n", projectRef,
                                                 e.getMessage(), plugin.toXML() ), e );
                    continue;
                }

                List<PluginDependencyView> pluginDependencies = null;
                Set<PluginDependencyView> impliedPluginDependencies = null;
                try
                {
                    pluginDependencies = plugin.getLocalPluginDependencies();
                    impliedPluginDependencies = plugin.getImpliedPluginDependencies();
                }
                catch ( final GalleyMavenException e )
                {
                    logger.error( String.format( "%s: Cannot retrieve plugin dependencies for: %s. Reason: %s",
                                                 projectRef, pluginRef, e.getMessage() ), e );
                }
                catch ( final InvalidVersionSpecificationException e )
                {
                    logger.error( String.format( "%s: Cannot retrieve plugin dependencies for: %s. Reason: %s",
                                                 projectRef, pluginRef, e.getMessage() ), e );
                }
                catch ( final InvalidRefException e )
                {
                    logger.error( String.format( "%s: Cannot retrieve plugin dependencies for: %s. Reason: %s",
                                                 projectRef, pluginRef, e.getMessage() ), e );
                }

                addPluginDependencies( pluginDependencies, plugin, pluginRef, projectRef, builder, source, managed );

                logger.debug( "{}: Adding implied dependencies for: {}\n\n  {}", projectRef, pluginRef,
                              impliedPluginDependencies == null ? "-NONE-" : new JoinString( "\n  ",
                                                                                             impliedPluginDependencies ) );
                addPluginDependencies( impliedPluginDependencies, plugin, pluginRef, projectRef, builder, source,
                                       managed );
            }
        }
    }

    private void addPluginDependencies( final Collection<PluginDependencyView> pluginDependencies,
                                        final PluginView plugin, final ProjectVersionRef pluginRef,
                                        final ProjectVersionRef projectRef, final Builder builder, final URI source,
                                        final boolean managed )
    {
        if ( pluginDependencies != null )
        {
            for ( final PluginDependencyView dep : pluginDependencies )
            {
                try
                {
                    final ProjectVersionRef ref = dep.asProjectVersionRef();

                    final String profileId = dep.getProfileId();
                    final URI location = RelationshipUtils.profileLocation( profileId );

                    final ArtifactRef artifactRef =
                        new SimpleArtifactRef( ref, dep.getType(), dep.getClassifier() );

                    // force the InvalidVersionSpecificationException.
                    artifactRef.getVersionSpec();

                    boolean inherited = dep.getOriginInfo().isInherited();
                    boolean mixin = dep.getOriginInfo().isMixin();
                    builder.withPluginDependencies( new SimplePluginDependencyRelationship(
                                                                                      source,
                                                                                      location,
                                                                                      projectRef,
                                                                                      pluginRef,
                                                                                      artifactRef,
                                                                                      builder.getNextPluginDependencyIndex( pluginRef,
                                                                                                                            managed,
                                                                                                                            inherited ),
                                                                                      managed,
                                                                                      inherited ) );
                }
                catch ( final InvalidRefException e )
                {
                    logger.error( String.format( "%s: plugin dependency is invalid in: %s! Reason: %s. Skipping:\n\n%s\n\n",
                                                 projectRef, pluginRef, e.getMessage(), dep.toXML() ), e );
                }
                catch ( final InvalidVersionSpecificationException e )
                {
                    logger.error( String.format( "%s: plugin dependency is invalid in: %s! Reason: %s. Skipping:\n\n%s\n\n",
                                                 projectRef, pluginRef, e.getMessage(), dep.toXML() ), e );
                }
                catch ( final GalleyMavenException e )
                {
                    logger.error( String.format( "%s: plugin dependency is invalid in: %s! Reason: %s. Skipping:\n\n%s\n\n",
                                                 projectRef, pluginRef, e.getMessage(), dep.toXML() ), e );
                }
            }
        }
    }

    protected void addDependencyRelationships( final URI source, final Builder builder, final MavenPomView pomView,
                                               final ProjectVersionRef projectRef,
                                               final boolean includeManagedDependencies )
    {
        // regardless of whether we're processing managed info, this is STRUCTURAL, so always grab it!
        List<DependencyView> boms = null;
        try
        {
            boms = pomView.getAllBOMs();
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            logger.error( String.format( "%s: Failed to retrieve BOM declarations: %s. Skipping", pomView.getRef(),
                                         e.getMessage() ), e );
        }
        catch ( final InvalidRefException e )
        {
            logger.error( String.format( "%s: Failed to retrieve BOM declarations: %s. Skipping", pomView.getRef(),
                                         e.getMessage() ), e );
        }

        if(boms!=null)
        {
            for ( int i = 0; i < boms.size(); i++ )
            {
                final DependencyView bomView = boms.get( i );
                try
                {
                    builder.withBoms( new SimpleBomRelationship( source, projectRef, bomView.asProjectVersionRef(), i,
                                                                 bomView.getOriginInfo().isInherited(),
                                                                 bomView.getOriginInfo().isMixin() ) );
                }
                catch ( final InvalidRefException e )
                {
                    logger.error( String.format( "%s dependency is invalid! Reason: %s. Skipping:\n\n%s\n\n", pomView.getRef(), e.getMessage(), bomView.toXML() ), e );
                }
                catch ( final InvalidVersionSpecificationException e )
                {
                    logger.error( String.format( "%s dependency is invalid! Reason: %s. Skipping:\n\n%s\n\n", pomView.getRef(), e.getMessage(), bomView.toXML() ), e );
                }
                catch ( final GalleyMavenException e )
                {
                    logger.error( String.format( "%s dependency is invalid! Reason: %s. Skipping:\n\n%s\n\n", pomView.getRef(), e.getMessage(), bomView.toXML() ), e );
                }
            }
        }

        if ( includeManagedDependencies )
        {
            List<DependencyView> deps = null;
            try
            {
                deps = pomView.getAllManagedDependencies();
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                logger.error( String.format( "%s: Failed to retrieve managed dependencies: %s. Skipping",
                                             pomView.getRef(), e.getMessage() ), e );
            }
            catch ( final InvalidRefException e )
            {
                logger.error( String.format( "%s: Failed to retrieve managed dependencies: %s. Skipping",
                                             pomView.getRef(), e.getMessage() ), e );
            }

            addDependencies( deps, projectRef, builder, source, true );
        }

        List<DependencyView> deps = null;
        try
        {
            deps = pomView.getAllDirectDependencies();
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            logger.error( String.format( "%s: Failed to retrieve direct dependencies: %s. Skipping", pomView.getRef(),
                                         e.getMessage() ), e );
        }
        catch ( final InvalidRefException e )
        {
            logger.error( String.format( "%s: Failed to retrieve direct dependencies: %s. Skipping", pomView.getRef(),
                                         e.getMessage() ), e );
        }

        addDependencies( deps, projectRef, builder, source, false );
    }

    private void addDependencies( final List<DependencyView> deps, final ProjectVersionRef projectRef,
                                  final Builder builder, final URI source, final boolean managed )
    {
        if ( deps != null )
        {
            for ( final DependencyView dep : deps )
            {
                try
                {
                    final ProjectVersionRef ref = dep.asProjectVersionRef();

                    final String profileId = dep.getProfileId();
                    final URI location = RelationshipUtils.profileLocation( profileId );

                    final ArtifactRef artifactRef =
                        new SimpleArtifactRef( ref, dep.getType(), dep.getClassifier() );

                    // force the InvalidVersionSpecificationException.
                    artifactRef.getVersionSpec();

                    Set<ProjectRefView> exclusionsView = dep.getExclusions();
                    ProjectRef[] excludes;
                    if ( exclusionsView != null && !exclusionsView.isEmpty() )
                    {
                        excludes = new ProjectRef[exclusionsView.size()];
                        int i = 0;
                        for ( ProjectRefView exclusionView : exclusionsView )
                        {
                            excludes[i] = exclusionView.asProjectRef();
                            i++;
                        }
                    }
                    else
                    {
                        excludes = new ProjectRef[0];
                    }

                    builder.withDependencies( new SimpleDependencyRelationship( source, location, projectRef, artifactRef,
                                                                          dep.getScope(),
                                                                          builder.getNextDependencyIndex( managed ),
                                                                          managed, dep.getOriginInfo().isInherited(),
                                                                          dep.isOptional(), excludes ) );
                }
                catch ( final InvalidRefException e )
                {
                    logger.error( String.format( "%s: dependency is invalid! Reason: %s. Skipping:\n\n%s\n\n",
                                                 projectRef, e.getMessage(), dep.toXML() ), e );
                }
                catch ( final InvalidVersionSpecificationException e )
                {
                    logger.error( String.format( "%s: dependency is invalid! Reason: %s. Skipping:\n\n%s\n\n",
                                                 projectRef, e.getMessage(), dep.toXML() ), e );
                }
                catch ( final GalleyMavenException e )
                {
                    logger.error( String.format( "%s: dependency is invalid! Reason: %s. Skipping:\n\n%s\n\n",
                                                 projectRef, e.getMessage(), dep.toXML() ), e );
                }
            }
        }
    }

    protected void addParentRelationship( final URI source, final Builder builder, final MavenPomView pomView,
                                          final ProjectVersionRef projectRef )
    {
        try
        {
            final ParentView parent = pomView.getParent();
            if ( parent != null )
            {
                final ProjectVersionRef ref = parent.asProjectVersionRef();
                // force the InvalidVersionSpecificationException.
                ref.getVersionSpec();

                logger.info( "Adding parent relationship for: {} to : {}", builder.getProjectRef(), ref );
                builder.withParent( new SimpleParentRelationship( source, builder.getProjectRef(), ref ) );
            }
            else
            {
                logger.info(
                        "Adding self-referential parent relationship for: {} to signify project has no parent, but is parsable.",
                        builder.getProjectRef() );
                builder.withParent( new SimpleParentRelationship( builder.getProjectRef() ) );
            }
        }
        catch ( final GalleyMavenException e )
        {
            logger.error( String.format( "%s: Parent reference is invalid! Reason: %s. Skipping.", projectRef,
                                         e.getMessage() ), e );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            logger.error( String.format( "%s: Parent reference is invalid! Reason: %s. Skipping.", projectRef,
                                         e.getMessage() ), e );
        }
        catch ( final InvalidRefException e )
        {
            logger.error( String.format( "%s: Parent reference is invalid! Reason: %s. Skipping.", projectRef,
                                         e.getMessage() ), e );
        }
    }

}
