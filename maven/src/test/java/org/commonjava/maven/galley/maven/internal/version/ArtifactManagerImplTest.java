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
package org.commonjava.maven.galley.maven.internal.version;

import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.atlas.maven.ident.ref.SimpleProjectVersionRef;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.maven.internal.metadata.StandardMetadataMapper;
import org.commonjava.maven.galley.maven.spi.metadata.MetadataMapper;
import org.commonjava.maven.galley.maven.testutil.TestFixture;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.testing.core.transport.job.TestDownload;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArtifactManagerImplTest
{

    private static final String URI = "test:artifact-manager";

    private static final Location LOCATION = new SimpleLocation( URI );

    private static final String ROOT = "artifact-manager/";

    @Rule
    public final TestFixture fixture = new TestFixture();

    @Test
    public void verifyConcreteResourceCreationFile()
                    throws Exception
    {
        final Location location = new SimpleLocation( "file:///home/foobar" );

        MetadataMapper m = new StandardMetadataMapper();
        ConcreteResource cr = m.createResource( location, null, null, "org.groupId" );
        assertEquals( "org/groupId/maven-metadata-local.xml", cr.getPath() );
    }

    @Test
    public void verifyConcreteResourceCreationHTTP()
                    throws Exception
    {
        final Location location = new SimpleLocation( "https:///home/foobar" );

        MetadataMapper m = new StandardMetadataMapper();
        ConcreteResource cr = m.createResource( location, null, null, "org.groupId" );
        assertEquals( "org/groupId/maven-metadata.xml", cr.getPath() );
    }

    @Test
    public void verifyConcreteResourceCreationFallback()
                    throws Exception
    {
        final Location location = new SimpleLocation( "test:///home/foobar" );

        MetadataMapper m = new StandardMetadataMapper();
        ConcreteResource cr = m.createResource( location, null, null, "org.groupId" );
        assertEquals( "org/groupId/maven-metadata.xml", cr.getPath() );
    }


    @Test
    public void resolveSnapshot_FirstMatch_SingletonLocationList_SingletonSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String base = "single-snapshot/";
        final String testResource = base + "single-snapshot.xml";
        final String testPomResource = base + "single-snapshot-pom.xml";

        final ProjectVersionRef ref = new SimpleProjectVersionRef( "org.group", "artifact", "1.0-SNAPSHOT" );
        final ConcreteResource metadataResource = new ConcreteResource( LOCATION, fixture.snapshotMetadataPath( ref ) );
        final ConcreteResource pomResource =
            new ConcreteResource( LOCATION, fixture.pomPath( ref.selectVersion( "1.0-20140604.101244-1" )
                                                                .asPomArtifact() ) );

        fixture.getTransport()
               .registerDownload( metadataResource, new TestDownload( ROOT + testResource ) );

        fixture.getTransport()
               .registerDownload( pomResource, new TestDownload( ROOT + testPomResource ) );

        final Transfer retrieved = fixture.getArtifactManager()
                                          .retrieve( LOCATION, ref.asPomArtifact(), new EventMetadata() );

        final Document document = fixture.getXml()
                                         .parse( retrieved, new EventMetadata() );
        final ProjectVersionRef result = fixture.getXml()
                                                .getProjectVersionRef( document );

        System.out.println( result );

        assertThat( result, notNullValue() );
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.101244-1" ) );
    }

    @Test
    public void resolveSnapshot_FirstMatch_SingletonLocationList_TwoSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String base = "2-snapshots-1-location/";
        final String testResource = base + "two-snapshots.xml";
        final String testPomResource = base + "two-snapshots-pom.xml";

        final ProjectVersionRef ref = new SimpleProjectVersionRef( "org.group2", "artifact", "1.0-SNAPSHOT" );
        final ConcreteResource metadataResource = new ConcreteResource( LOCATION, fixture.snapshotMetadataPath( ref ) );
        final ConcreteResource pomResource =
            new ConcreteResource( LOCATION, fixture.pomPath( ref.selectVersion( "1.0-20140604.102909-1" )
                                                                .asPomArtifact() ) );

        fixture.getTransport()
               .registerDownload( metadataResource, new TestDownload( ROOT + testResource ) );

        fixture.getTransport()
               .registerDownload( pomResource, new TestDownload( ROOT + testPomResource ) );

        final Transfer retrieved = fixture.getArtifactManager()
                                          .retrieve( LOCATION, ref.asPomArtifact(), new EventMetadata() );

        final Document document = fixture.getXml()
                                         .parse( retrieved, new EventMetadata() );
        final ProjectVersionRef result = fixture.getXml()
                                                .getProjectVersionRef( document );

        System.out.println( result );

        assertThat( result, notNullValue() );
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.102909-1" ) );
    }

}
