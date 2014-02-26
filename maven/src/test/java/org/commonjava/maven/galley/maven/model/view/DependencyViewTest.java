/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.maven.model.view;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.commonjava.maven.atlas.ident.DependencyScope;
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
        assertThat( managed.size(), equalTo( 1 ) );
        final DependencyView dv = managed.get( 0 );
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

}
