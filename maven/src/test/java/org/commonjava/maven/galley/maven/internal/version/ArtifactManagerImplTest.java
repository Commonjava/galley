package org.commonjava.maven.galley.maven.internal.version;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.testutil.TestFixture;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.testing.core.transport.job.TestDownload;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;

public class ArtifactManagerImplTest
{

    private static final String URI = "test:artifact-manager";

    private static final Location LOCATION = new SimpleLocation( URI );

    private static final String BASE = "artifact-manager/";

    @Rule
    public TestFixture fixture = new TestFixture();

    @Before
    public void before()
    {
        fixture.initMissingComponents();
    }

    @Test
    public void resolveSnapshot_FirstMatch_SingletonLocationList_SingletonSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String testResource = "single-snapshot.xml";
        final String testPomResource = "single-snapshot-pom.xml";

        final ProjectVersionRef ref = new ProjectVersionRef( "org.group", "artifact", "1.0-SNAPSHOT" );
        final ConcreteResource metadataResource = new ConcreteResource( LOCATION, fixture.snapshotMetadataPath( ref ) );
        final ConcreteResource pomResource =
            new ConcreteResource( LOCATION, fixture.pomPath( ref.selectVersion( "1.0-20140604.101244-1" )
                                                                .asPomArtifact() ) );

        fixture.getTransport()
               .registerDownload( metadataResource, new TestDownload( BASE + testResource ) );

        fixture.getTransport()
               .registerDownload( pomResource, new TestDownload( BASE + testPomResource ) );

        final Transfer retrieved = fixture.getArtifactManager()
                                          .retrieve( LOCATION, ref.asPomArtifact() );

        final Document document = fixture.getXml()
                                         .parse( retrieved );
        final ProjectVersionRef result = fixture.getXml()
                                                .getProjectVersionRef( document );

        System.out.println( result );

        //        assertThat( result, notNullValue() );
        //        assertThat( result.getVersionString(), equalTo( "1.0-20140604.101244-1" ) );
    }

    @Test
    public void resolveSnapshot_FirstMatch_SingletonLocationList_TwoSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String testResource = "two-snapshots.xml";
        final String testPomResource = "two-snapshots-pom.xml";

        final ProjectVersionRef ref = new ProjectVersionRef( "org.group2", "artifact", "1.0-SNAPSHOT" );
        final ConcreteResource metadataResource = new ConcreteResource( LOCATION, fixture.snapshotMetadataPath( ref ) );
        final ConcreteResource pomResource =
            new ConcreteResource( LOCATION, fixture.pomPath( ref.selectVersion( "1.0-20140604.102909-1" )
                                                                .asPomArtifact() ) );

        fixture.getTransport()
               .registerDownload( metadataResource, new TestDownload( BASE + testResource ) );

        fixture.getTransport()
               .registerDownload( pomResource, new TestDownload( BASE + testPomResource ) );

        final Transfer retrieved = fixture.getArtifactManager()
                                          .retrieve( LOCATION, ref.asPomArtifact() );

        final Document document = fixture.getXml()
                                         .parse( retrieved );
        final ProjectVersionRef result = fixture.getXml()
                                                .getProjectVersionRef( document );

        System.out.println( result );

        assertThat( result, notNullValue() );
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.102909-1" ) );
    }

}
