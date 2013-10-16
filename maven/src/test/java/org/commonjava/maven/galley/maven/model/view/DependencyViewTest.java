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

        logger.info( "%s", dv.asProjectRef() );
        logger.info( "%s", dv.asProjectVersionRef()
                             .getVersionSpec() );
        logger.info( "%s", dv.asVersionlessArtifactRef() );
        logger.info( "%s", dv.asArtifactRef()
                             .getVersionSpec() );
    }

    @Test
    public void dependencyVersionExpressionWithDotsInParentPom()
        throws Exception
    {
        final DependencyView dv = loadFirstDirectDependency( "dep-expr-in-parent.pom.xml", "dep-expr-parent.pom.xml" );

        logger.info( "%s", dv.asProjectRef() );
        logger.info( "%s", dv.asProjectVersionRef()
                             .getVersionSpec() );
        logger.info( "%s", dv.asVersionlessArtifactRef() );
        logger.info( "%s", dv.asArtifactRef()
                             .getVersionSpec() );
    }

    @Test
    public void managedDependencyVersionExpressionWithDotsInParentPom()
        throws Exception
    {
        final DependencyView dv = loadFirstManagedDependency( "managed-dep-expr-in-parent.pom.xml", "dep-expr-parent.pom.xml" );

        logger.info( "%s", dv.asProjectRef() );
        logger.info( "%s", dv.asProjectVersionRef()
                             .getVersionSpec() );
        logger.info( "%s", dv.asVersionlessArtifactRef() );
        logger.info( "%s", dv.asArtifactRef()
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
        //        logger.info( "%s", dv.asProjectRef() );
        //        logger.info( "%s", dv.asProjectVersionRef()
        //                             .getVersionSpec() );
        //        logger.info( "%s", dv.asVersionlessArtifactRef() );
        //        logger.info( "%s", dv.asArtifactRef()
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
        //        logger.info( "%s", dv.asProjectRef() );
        //        logger.info( "%s", dv.asProjectVersionRef()
        //                             .getVersionSpec() );
        //        logger.info( "%s", dv.asVersionlessArtifactRef() );
        //        logger.info( "%s", dv.asArtifactRef()
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
