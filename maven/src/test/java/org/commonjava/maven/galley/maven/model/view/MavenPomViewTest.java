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
package org.commonjava.maven.galley.maven.model.view;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import org.commonjava.maven.atlas.ident.DependencyScope;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.parse.GalleyMavenXMLException;
import org.junit.Test;

public class MavenPomViewTest
    extends AbstractMavenViewTest
{

    @Override
    protected String getBaseResource()
    {
        return "view/pom/";
    }

    @Test
    public void dependencyManagedBySingleBOM()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-bom.xml" );
        final MavenPomView bomView = loadPoms( "simple-bom.xml" );

        pomView.addMixin( new MavenXmlMixin<ProjectVersionRef>( bomView, MavenXmlMixin.DEPENDENCY_MIXIN ) );

        final DependencyView dv = pomView.getAllDirectDependencies()
                                         .get( 0 );

        assertThat( dv.getVersion(), equalTo( "1.0" ) );
        assertThat( dv.getScope(), equalTo( DependencyScope.test ) );
    }

    @Test
    public void dependencyManagedBySingleBOMWithExpressionToProjectGroupId()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-bom-expr.xml" );
        final MavenPomView bomView = loadPoms( "simple-bom-expr.xml" );

        pomView.addMixin( new MavenXmlMixin<ProjectVersionRef>( bomView, MavenXmlMixin.DEPENDENCY_MIXIN ) );

        final DependencyView dv = pomView.getAllDirectDependencies()
                                         .get( 0 );

        assertThat( dv.getVersion(), equalTo( "1.0" ) );
        assertThat( dv.getScope(), equalTo( DependencyScope.test ) );
    }

    @Test
    public void projectWithBOMContainingBOMs()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-bom-of-boms.xml" );
        final MavenPomView bomView = loadPoms( "bom-with-boms.xml" );

        pomView.addMixin( new MavenXmlMixin<ProjectVersionRef>( bomView, MavenXmlMixin.DEPENDENCY_MIXIN ) );

        final List<DependencyView> boms = pomView.getAllBOMs();

        System.out.printf( "Found {} boms\n\n", boms.size() );
        for ( final DependencyView bom : boms )
        {
            System.out.println( bom.asProjectVersionRef() );
        }
    }

    @Test
    public void retrieveManagedDependencyFromBOMWithExpressionToBOMGroupId()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-bom-expr.xml" );
        final MavenPomView bomView = loadPoms( "simple-bom-expr.xml" );

        pomView.addMixin( new MavenXmlMixin<ProjectVersionRef>( bomView, MavenXmlMixin.DEPENDENCY_MIXIN ) );

        DependencyView dv = pomView.getAllManagedDependencies()
                                   .get( 0 );

        assertThat( dv.getGroupId(), equalTo( "org.foo" ) );

        dv = pomView.getAllBOMs()
                    .get( 0 );

        assertThat( dv.getGroupId(), equalTo( "org.foo" ) );
    }

    @Test
    public void resolveParentVersionExpressionWithoutProjectPrefix()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-parent-expr.xml"/*, "simple-parent-pom.xml"*/);

        final String value = pomView.resolveExpressions( "${parent.version}" );

        assertThat( value, equalTo( "1.0.0.0" ) );
    }

    @Test
    public void resolveExpressionWithDeprecatedPomDotPrefix()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-parent-expr.xml"/*, "simple-parent-pom.xml"*/);

        final String value = pomView.resolveExpressions( "${pom.parent.version}" );

        assertThat( value, equalTo( "1.0.0.0" ) );
    }

    @Test
    public void parentPathIsLocalOnly()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-broken-parent.xml", "pom-with-parent.xml", "simple-parent-pom.xml" );

        final String value = pomView.resolveExpressions( "${parent.version}" );

        assertThat( pomView.containsExpression( value ), equalTo( true ) );
    }

    @Test
    public void groupIdFailOverToParent()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-parent-groupId.xml", "simple-parent-pom.xml" );

        final String gid = pomView.getGroupId();
        final ProjectVersionRef pvr = pomView.asProjectVersionRef();

        assertThat( gid, equalTo( "org.foo" ) );
        assertThat( pvr.getGroupId(), equalTo( "org.foo" ) );
    }

    @Test( expected = GalleyMavenXMLException.class )
    public void artifactId_DOES_NOT_FailOverToParent()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-broken-artifactId.xml", "pom-with-parent.xml", "simple-parent-pom.xml" );

        final String aid = pomView.getArtifactId();

        assertThat( aid, nullValue() );

        pomView.asProjectVersionRef();
    }

    @Test
    public void retrieveDirectBOMReference()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-bom.xml" );
        final List<DependencyView> boms = pomView.getAllBOMs();

        assertThat( boms, notNullValue() );
        assertThat( boms.size(), equalTo( 1 ) );
    }

    @Test
    public void retrieveBOMReferenceInParent()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-bom-child.xml", "pom-with-bom.xml" );
        final List<DependencyView> boms = pomView.getAllBOMs();

        assertThat( boms, notNullValue() );
        assertThat( boms.size(), equalTo( 1 ) );
    }

    @Test
    public void retrieveImpliedPluginDepsForSurefire()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-surefire.xml" );

        final List<PluginView> plugins = pomView.getAllBuildPlugins();
        assertThat( plugins, notNullValue() );
        assertThat( plugins.size(), equalTo( 1 ) );

        final PluginView pv = plugins.get( 0 );
        assertThat( pv, notNullValue() );

        final Set<PluginDependencyView> ipdvs = pv.getImpliedPluginDependencies();
        assertThat( ipdvs, notNullValue() );
        assertThat( ipdvs.size(), equalTo( 5 ) );

    }

    @Test
    public void artifactIdWithWhitespace()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-whitespace-artifactId.xml" );

        final String aid = pomView.getArtifactId();
        final ProjectVersionRef pvr = pomView.asProjectVersionRef();

        assertThat( aid, equalTo( "bar-project" ) );
        assertThat( pvr.getArtifactId(), equalTo( "bar-project" ) );
    }

}
