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
import org.commonjava.atlas.maven.graph.rel.ProjectRelationship;
import org.commonjava.atlas.maven.graph.rel.RelationshipType;
import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.atlas.maven.ident.ref.SimpleProjectVersionRef;
import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.maven.galley.maven.model.view.DependencyView;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.model.view.PluginDependencyView;
import org.commonjava.maven.galley.maven.model.view.PluginView;
import org.commonjava.maven.galley.maven.model.view.RepositoryView;
import org.commonjava.maven.galley.maven.testutil.TestFixture;
import org.commonjava.maven.galley.maven.util.ArtifactPathUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.testing.core.transport.job.TestDownload;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class MavenModelProcessorTest
{

    private static final String PROJ_BASE = "pom-processor/";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public final TestFixture fixture = new TestFixture();

    @Test
    public void resolvePluginVersionFromManagementExpression()
        throws Exception
    {
        final URI src = new URI( "http://nowhere.com/path/to/repo" );

        final ProjectVersionRef childRef = new SimpleProjectVersionRef( "org.test", "test-child", "1.0" );

        final LinkedHashMap<ProjectVersionRef, String> lineage = new LinkedHashMap<>();
        lineage.put( childRef, "child.pom.xml" );
        lineage.put( new SimpleProjectVersionRef( "org.test", "test-parent", "1.0" ), "parent.pom.xml" );

        final Location location = new SimpleLocation( "test", src.toString(), false, true, true, false, true, false );

        final String base = PROJ_BASE + "version-expression-managed-parent-plugin/";

        for ( final Entry<ProjectVersionRef, String> entry : lineage.entrySet() )
        {
            final ProjectVersionRef ref = entry.getKey();
            final String filename = entry.getValue();

            final String path = ArtifactPathUtils.formatArtifactPath( ref.asPomArtifact(), fixture.getTypeMapper() );

            fixture.getTransport()
                   .registerDownload( new ConcreteResource( location, path ), new TestDownload( base + filename ) );
        }

        final Transfer transfer = fixture.getArtifactManager()
                                         .retrieve( location, childRef.asPomArtifact() );

        final MavenPomView pomView = fixture.getPomReader()
                                            .read( childRef, transfer, Collections.singletonList( location ) );

        final List<PluginView> buildPlugins = pomView.getAllBuildPlugins();

        assertThat( buildPlugins, notNullValue() );
        assertThat( buildPlugins.size(), equalTo( 1 ) );

        final PluginView pv = buildPlugins.get( 0 );
        assertThat( pv, notNullValue() );
        assertThat( pv.getVersion(), equalTo( "1.0" ) );

        final ModelProcessorConfig discoveryConfig = new ModelProcessorConfig();
        discoveryConfig.setIncludeManagedDependencies( true );
        discoveryConfig.setIncludeBuildSection( true );
        discoveryConfig.setIncludeManagedPlugins( false );

        EProjectDirectRelationships result =
                fixture.getModelProcessor().readRelationships( pomView, src, discoveryConfig );

        final Set<ProjectRelationship<?, ?>> rels = result.getExactAllRelationships();

        logger.info( "Found {} relationships:\n\n  {}", rels.size(), new JoinString( "\n  ", rels ) );

        boolean seen = false;
        for ( final ProjectRelationship<?, ?> rel : rels )
        {
            if ( rel.getType() == RelationshipType.PLUGIN && !rel.isManaged() )
            {
                if ( seen )
                {
                    fail( "Multiple plugins found!" );
                }

                seen = true;
                assertThat( rel.getTarget()
                               .getVersionString(), equalTo( "1.0" ) );
            }
        }

        if ( !seen )
        {
            fail( "Plugin relationship not found!" );
        }
    }

    @Test
    public void resolvePluginDependencyFromManagedInfo()
        throws Exception
    {
        final URI src = new URI( "http://nowhere.com/path/to/repo" );

        final ProjectVersionRef childRef = new SimpleProjectVersionRef( "org.test", "test-child", "1.0" );

        final LinkedHashMap<ProjectVersionRef, String> lineage = new LinkedHashMap<>();
        lineage.put( childRef, "child.pom.xml" );
        lineage.put( new SimpleProjectVersionRef( "org.test", "test-parent", "1.0" ), "parent.pom.xml" );

        final Location location = new SimpleLocation( "test", src.toString(), false, true, true, false, true, false );

        final String base = PROJ_BASE + "dependency-in-managed-parent-plugin/";

        for ( final Entry<ProjectVersionRef, String> entry : lineage.entrySet() )
        {
            final ProjectVersionRef ref = entry.getKey();
            final String filename = entry.getValue();

            final String path = ArtifactPathUtils.formatArtifactPath( ref.asPomArtifact(), fixture.getTypeMapper() );

            fixture.getTransport()
                   .registerDownload( new ConcreteResource( location, path ), new TestDownload( base + filename ) );
        }

        final Transfer transfer = fixture.getArtifactManager()
                                         .retrieve( location, childRef.asPomArtifact() );

        final MavenPomView pomView = fixture.getPomReader()
                                            .read( childRef, transfer, Collections.singletonList( location ) );

        final List<PluginView> buildPlugins = pomView.getAllBuildPlugins();

        assertThat( buildPlugins, notNullValue() );
        assertThat( buildPlugins.size(), equalTo( 1 ) );

        final PluginView pv = buildPlugins.get( 0 );
        assertThat( pv, notNullValue() );

        final List<PluginDependencyView> deps = pv.getLocalPluginDependencies();
        assertThat( deps, notNullValue() );
        assertThat( deps.size(), equalTo( 1 ) );

        final PluginDependencyView pdv = deps.get( 0 );
        assertThat( pdv, notNullValue() );
        assertThat( pdv.asArtifactRef()
                       .getVersionString(), equalTo( "1.0" ) );

        final ModelProcessorConfig discoveryConfig = new ModelProcessorConfig();
        discoveryConfig.setIncludeManagedDependencies( true );
        discoveryConfig.setIncludeBuildSection( true );
        discoveryConfig.setIncludeManagedPlugins( false );

        EProjectDirectRelationships result =
                fixture.getModelProcessor().readRelationships( pomView, src, discoveryConfig );

        final Set<ProjectRelationship<?, ?>> rels = result.getExactAllRelationships();

        logger.info( "Found {} relationships:\n\n  {}", rels.size(), new JoinString( "\n  ", rels ) );

        boolean seen = false;
        for ( final ProjectRelationship<?, ?> rel : rels )
        {
            if ( rel.getType() == RelationshipType.PLUGIN_DEP && !rel.isManaged() )
            {
                if ( seen )
                {
                    fail( "Multiple plugin dependencies found!" );
                }

                seen = true;
                assertThat( rel.getTarget()
                               .getVersionString(), equalTo( "1.0" ) );
            }
        }

        if ( !seen )
        {
            fail( "Plugin-dependency relationship not found!" );
        }
    }

    @Test
    public void resolvePluginVersionFromPropertyInProfile()
        throws Exception
    {
        final URI src = new URI( "http://nowhere.com/path/to/repo" );

        final ProjectVersionRef childRef =
            new SimpleProjectVersionRef( "org.test", "test-pom", "1.0" );

        final LinkedHashMap<ProjectVersionRef, String> lineage = new LinkedHashMap<>();
        lineage.put( childRef, "test-pom-1.0.pom.xml" );

        final Location location = new SimpleLocation( "test", src.toString(), false, true, true, false, true, false );

        final String base = PROJ_BASE + "version-expression-in-a-profile/";

        for ( final Entry<ProjectVersionRef, String> entry : lineage.entrySet() )
        {
            final ProjectVersionRef ref = entry.getKey();
            final String filename = entry.getValue();

            final String path = ArtifactPathUtils.formatArtifactPath( ref.asPomArtifact(), fixture.getTypeMapper() );

            fixture.getTransport()
                   .registerDownload( new ConcreteResource( location, path ), new TestDownload( base + filename ) );
        }

        final Transfer transfer = fixture.getArtifactManager()
                                         .retrieve( location, childRef.asPomArtifact() );

        final MavenPomView pomView = fixture.getPomReader()
                                            .read( childRef, transfer, Collections.singletonList( location ) );

        final List<PluginView> buildPlugins = pomView.getAllBuildPlugins();

        assertThat( buildPlugins, notNullValue() );
        assertThat( buildPlugins.size(), equalTo( 1 ) );

        final PluginView pv = buildPlugins.get( 0 );
        assertThat( pv, notNullValue() );
        assertThat( pv.getVersion(), equalTo( "2.0" ) );

        final ModelProcessorConfig discoveryConfig = new ModelProcessorConfig();
        discoveryConfig.setIncludeManagedDependencies( true );
        discoveryConfig.setIncludeBuildSection( true );
        discoveryConfig.setIncludeManagedPlugins( false );

        EProjectDirectRelationships result =
                fixture.getModelProcessor().readRelationships( pomView, src, discoveryConfig );

        final Set<ProjectRelationship<?, ?>> rels = result.getExactAllRelationships();

        logger.info( "Found {} relationships:\n\n  {}", rels.size(), new JoinString( "\n  ", rels ) );

        boolean seen = false;
        for ( final ProjectRelationship<?, ?> rel : rels )
        {
            if ( rel.getType() == RelationshipType.PLUGIN && !rel.isManaged() )
            {
                if ( seen )
                {
                    fail( "Multiple plugins found!" );
                }

                seen = true;
                assertThat( rel.getTarget()
                               .getVersionString(), equalTo( "2.0" ) );
            }
        }

        if ( !seen )
        {
            fail( "Plugin relationship not found!" );
        }
    }

    @Test
    public void resolveRepositoriesExpressionFromPropertyInProfile()
            throws Exception
    {
        final URI src = new URI( "http://nowhere.com/path/to/repo" );

        final ProjectVersionRef childRef = new SimpleProjectVersionRef( "org.test", "test-pom", "1.0" );

        final LinkedHashMap<ProjectVersionRef, String> lineage = new LinkedHashMap<>();
        lineage.put( childRef, "test-pom-1.0.pom.xml" );

        final Location location = new SimpleLocation( "test", src.toString(), false, true, true, false, true, false );

        final String base = PROJ_BASE + "resolve-expression-in-a-profile/";

        for ( final Entry<ProjectVersionRef, String> entry : lineage.entrySet() )
        {
            final ProjectVersionRef ref = entry.getKey();
            final String filename = entry.getValue();

            final String path = ArtifactPathUtils.formatArtifactPath( ref.asPomArtifact(), fixture.getTypeMapper() );

            fixture.getTransport()
                   .registerDownload( new ConcreteResource( location, path ), new TestDownload( base + filename ) );
        }

        final Transfer transfer = fixture.getArtifactManager().retrieve( location, childRef.asPomArtifact() );

        final MavenPomView pomView =
                fixture.getPomReader().read( childRef, transfer, Collections.singletonList( location ) );

        final List<RepositoryView> rvs = pomView.getAllRepositories();

        assertThat( rvs, notNullValue() );
        assertThat( rvs.size(), equalTo( 3 ) );
        assertThat( rvs.get( 0 ).getName(), equalTo( "repo.one" ) );
        assertThat( rvs.get( 0 ).getUrl(), equalTo( "http://repo.one.repository" ) );
        assertThat( rvs.get( 1 ).getName(), equalTo( "test.oracle.repo" ) );
        assertThat( rvs.get( 1 ).getUrl(), equalTo( "http://test.oracle.repository" ) );
        assertThat( rvs.get( 2 ).getName(), equalTo( "test.second.oracle.repo" ) );
        assertThat( rvs.get( 2 ).getUrl(), equalTo( "http://another.test.two.oracle.repository" ) );
        }

    @Test
    public void resolveExpressionsBothInDepAndProfile()
            throws Exception
    {
        final URI src = new URI( "http://nowhere.com/path/to/repo" );

        final ProjectVersionRef childRef = new SimpleProjectVersionRef( "org.test", "test-pom", "1.0" );

        final LinkedHashMap<ProjectVersionRef, String> lineage = new LinkedHashMap<>();
        lineage.put( childRef, "test-pom-1.0.pom.xml" );

        final Location location = new SimpleLocation( "test", src.toString(), false, true, true, false, true, false );

        final String base = PROJ_BASE + "resolve-expression-in-a-profile/";

        for ( final Entry<ProjectVersionRef, String> entry : lineage.entrySet() )
        {
            final ProjectVersionRef ref = entry.getKey();
            final String filename = entry.getValue();

            final String path = ArtifactPathUtils.formatArtifactPath( ref.asPomArtifact(), fixture.getTypeMapper() );

            fixture.getTransport()
                   .registerDownload( new ConcreteResource( location, path ), new TestDownload( base + filename ) );
        }

        final Transfer transfer = fixture.getArtifactManager().retrieve( location, childRef.asPomArtifact() );

        final MavenPomView pomView =
                fixture.getPomReader().read( childRef, transfer, Collections.singletonList( location ) );

        String url = pomView.resolveExpressions( "${repo.url}", "test.oracle" );

        assertThat( url, equalTo( "http://test.oracle.repository" ) );

        List<DependencyView> dvs = pomView.getAllDirectDependencies();
        assertThat( dvs.get( 0 ).getVersion(), equalTo( "2.5" ) );

        String version = pomView.resolveExpressions( "${commons.lang.value}" );
        assertThat( version, equalTo( "2.5" ) );
    }
}
