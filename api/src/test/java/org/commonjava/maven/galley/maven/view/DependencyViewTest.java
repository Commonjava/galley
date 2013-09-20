package org.commonjava.maven.galley.maven.view;

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

}
