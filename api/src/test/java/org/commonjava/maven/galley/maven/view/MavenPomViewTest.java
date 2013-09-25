package org.commonjava.maven.galley.maven.view;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.commonjava.maven.atlas.ident.DependencyScope;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
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

    @Test( expected = IllegalArgumentException.class )
    public void artifactId_DOES_NOT_FailOverToParent()
        throws Exception
    {
        final MavenPomView pomView = loadPoms( "pom-with-broken-artifactId.xml", "pom-with-parent.xml", "simple-parent-pom.xml" );

        final String aid = pomView.getArtifactId();

        assertThat( aid, nullValue() );

        pomView.asProjectVersionRef();
    }

}
