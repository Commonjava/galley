package org.commonjava.maven.galley.maven.internal.version;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.testutil.TestFixture;
import org.commonjava.maven.galley.maven.version.LatestVersionSelectionStrategy;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.testing.core.transport.job.TestDownload;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class VersionResolverImplTest
{

    private static final String URI = "test:version-resolver";

    private static final Location LOCATION = new SimpleLocation( URI );

    private static final List<? extends Location> LOCATIONS = Collections.singletonList( LOCATION );

    private static final String BASE = "version-resolver/";

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

        final ProjectVersionRef ref = new ProjectVersionRef( "org.group", "artifact", "1.0-SNAPSHOT" );
        final ConcreteResource cr = new ConcreteResource( LOCATION, fixture.snapshotMetadataPath( ref ) );
        final TestDownload download = new TestDownload( BASE + testResource );

        fixture.getTransport()
               .registerDownload( cr, download );

        final ProjectVersionRef result =
            fixture.getVersionResolver()
                   .resolveFirstMatchVariableVersion( LOCATIONS, ref, LatestVersionSelectionStrategy.INSTANCE );

        assertThat( result, notNullValue() );
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.101244-1" ) );
    }

    @Test
    public void resolveSnapshot_FirstMatch_SingletonLocationList_TwoSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String testResource = "two-snapshots.xml";

        final ProjectVersionRef ref = new ProjectVersionRef( "org.group2", "artifact", "1.0-SNAPSHOT" );
        final ConcreteResource cr = new ConcreteResource( LOCATION, fixture.snapshotMetadataPath( ref ) );
        final TestDownload download = new TestDownload( BASE + testResource );

        fixture.getTransport()
               .registerDownload( cr, download );

        final ProjectVersionRef result =
            fixture.getVersionResolver()
                   .resolveFirstMatchVariableVersion( LOCATIONS, ref, LatestVersionSelectionStrategy.INSTANCE );

        assertThat( result, notNullValue() );
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.102909-1" ) );
    }

}
