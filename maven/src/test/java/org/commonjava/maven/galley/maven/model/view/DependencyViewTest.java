/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.commonjava.atlas.maven.ident.DependencyScope;
import org.commonjava.atlas.maven.ident.ref.VersionlessArtifactRef;
import org.junit.Test;

public class DependencyViewTest
    extends AbstractMavenViewTest
{

    @Override
    protected String getBaseResource()
    {
        return "view/dep/";
    }

    @Test
    public void selfContainedDependency()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "simple.pom.xml" );

        dv.asProjectRef();
        dv.asProjectVersionRef()
          .getVersionSpec();
        dv.asVersionlessArtifactRef();
        dv.asArtifactRef()
          .getVersionSpec();

    }

    @Test
    public void selfContainedDependency_VersionExpression()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "simple-expression.pom.xml" );

        dv.asProjectRef();
        dv.asProjectVersionRef()
          .getVersionSpec();
        dv.asVersionlessArtifactRef();
        dv.asArtifactRef()
          .getVersionSpec();

    }

    @Test
    public void selfContainedDependency_VersionExpressionWithDots()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "simple-expression-dots.pom.xml" );

        logger.info( "{}", dv.asProjectRef() );
        logger.info( "{}", dv.asProjectVersionRef()
                             .getVersionSpec() );
        logger.info( "{}", dv.asVersionlessArtifactRef() );
        logger.info( "{}", dv.asArtifactRef()
                             .getVersionSpec() );
    }

    @Test
    public void dependencyVersionExpressionWithDotsInParentPom()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "dep-expr-in-parent.pom.xml", "dep-expr-parent.pom.xml" );

        logger.info( "{}", dv.asProjectRef() );
        logger.info( "{}", dv.asProjectVersionRef()
                             .getVersionSpec() );
        logger.info( "{}", dv.asVersionlessArtifactRef() );
        logger.info( "{}", dv.asArtifactRef()
                             .getVersionSpec() );
    }

    @Test
    public void managedDependencyVersionExpressionWithDotsInParentPom()
        throws Exception
    {
        final DependencyView dv = loadFirstManagedDependency( "managed-dep-expr-in-parent.pom.xml", "dep-expr-parent.pom.xml" );

        logger.info( "{}", dv.asProjectRef() );
        logger.info( "{}", dv.asProjectVersionRef()
                             .getVersionSpec() );
        logger.info( "{}", dv.asVersionlessArtifactRef() );
        logger.info( "{}", dv.asArtifactRef()
                             .getVersionSpec() );
    }

    @Test
    public void managedDependency_ExpressionPropertyInBOMsParent()
        throws Exception
    {
        // FIXME: POM <dep> -> parent -> BOM <dep> -> parent/${expression}
        final DependencyView dv =
            loadFirstDirectDependency( "child-with-bom-in-parent.xml", "parent-with-bom.xml", "bom-with-version-in-parent.xml",
                                       "parent-with-versions.xml" );

        logger.info( dv.asProjectVersionRef()
                       .toString() );
        //        logger.info( "{}", dv.asProjectRef() );
        //        logger.info( "{}", dv.asProjectVersionRef()
        //                             .getVersionSpec() );
        //        logger.info( "{}", dv.asVersionlessArtifactRef() );
        //        logger.info( "{}", dv.asArtifactRef()
        //                             .getVersionSpec() );
    }

    @Test
    public void managedDependency_ExpressionPropertyInBOMsParentWithPropRelocation()
        throws Exception
    {
        // FIXME: POM <dep> -> parent -> BOM <dep> -> parent/${expression}
        final DependencyView dv =
            loadFirstDirectDependency( "child-with-bom-in-parent.xml", "parent-with-bom.xml", "bom-with-version-in-parent.xml",
                                       "parent-with-versions-relocated.xml" );

        logger.info( dv.asProjectVersionRef()
                       .toString() );
        //        logger.info( "{}", dv.asProjectRef() );
        //        logger.info( "{}", dv.asProjectVersionRef()
        //                             .getVersionSpec() );
        //        logger.info( "{}", dv.asVersionlessArtifactRef() );
        //        logger.info( "{}", dv.asArtifactRef()
        //                             .getVersionSpec() );
    }

    @Test
    public void managedDependency()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "managed.pom.xml" );

        dv.asProjectRef();
        dv.asProjectVersionRef()
          .getVersionSpec();

        dv.asVersionlessArtifactRef();
        dv.asArtifactRef()
          .getVersionSpec();

    }

    @Test
    public void bomDependencyInParent()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "managed-depless-child.pom.xml", "managed-bom-parent.pom.xml" );
        final List<DependencyView> managed = pomView.getAllManagedDependencies();
        assertThat( managed.size(), equalTo( 0 ) );

        final List<DependencyView> boms = pomView.getAllBOMs();
        assertThat( boms.size(), equalTo( 1 ) );
        final DependencyView dv = boms.get( 0 );
        assertThat( dv.getScope(), equalTo( DependencyScope._import ) );
    }

    @Test
    public void managedDependencyInProfile()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "managed-in-profile.pom.xml" );

        dv.asProjectRef();
        dv.asProjectVersionRef()
          .getVersionSpec();

        dv.asVersionlessArtifactRef();
        dv.asArtifactRef()
          .getVersionSpec();

    }

    @Test
    public void managedDependencyOutsideProfile()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "managed-outside-profile.pom.xml" );

        dv.asProjectRef();
        dv.asProjectVersionRef()
          .getVersionSpec();

        dv.asVersionlessArtifactRef();
        dv.asArtifactRef()
          .getVersionSpec();

    }

    @Test
    public void managedDependencyInParent()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "managed-child.pom.xml", "managed-parent.pom.xml" );

        dv.asProjectRef();
        dv.asProjectVersionRef()
          .getVersionSpec();

        dv.asVersionlessArtifactRef();
        dv.asArtifactRef()
          .getVersionSpec();

    }

    @Test
    public void managedDependencyWithProjectVersionExpressionInParent()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "managed-child.pom.xml", "managed-dep-project-property-in-parent.pom.xml" );

        dv.asProjectRef();
        dv.asProjectVersionRef()
          .getVersionSpec();

        dv.asVersionlessArtifactRef();
        dv.asArtifactRef()
          .getVersionSpec();

        assertThat( dv.asArtifactRef()
                      .getVersionString(), equalTo( "1.0" ) );

    }

    @Test
    public void managedDependencyWithTwoTypeEntriesInParent()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "managed-child.pom.xml", "managed-dep-two-types-in-parent.pom.xml" );

        dv.asProjectRef();
        dv.asProjectVersionRef()
          .getVersionSpec();

        dv.asVersionlessArtifactRef();
        dv.asArtifactRef()
          .getVersionSpec();

        assertThat( dv.getScope(), equalTo( DependencyScope.compile ) );

    }

    @Test
    public void managedDependency_FirstBOMWins()
        throws Exception
    {
        final DependencyView dv =
            loadFirstDirectDependency( "child-with-two-boms.xml", "bom-with-first-version.xml",
                                       "bom-with-second-version.xml" );

        logger.info( dv.asProjectVersionRef()
                       .toString() );

        assertThat( dv.getVersion(), equalTo( "1.1" ) );
    }

    @Test
    public void getAllManagedDependencies_FirstBOMWinsOnConflict()
        throws Exception
    {
        final List<DependencyView> dvs =
            loadAllManagedDependencies( "child-with-two-boms.xml", "bom-with-first-version.xml",
                                        "bom-with-second-version.xml" );

        final Set<VersionlessArtifactRef> seen = new HashSet<VersionlessArtifactRef>();
        for ( final DependencyView dependencyView : dvs )
        {
            final VersionlessArtifactRef var = dependencyView.asVersionlessArtifactRef();
            logger.info( var + ": " + dependencyView.asArtifactRef() );
            assertThat( "Collision NOT resolved: " + var, seen.contains( var ), equalTo( false ) );
            seen.add( var );
        }
    }

}
