/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.transport.htcli.internal;

import org.apache.commons.io.FileUtils;
import org.commonjava.maven.galley.cache.testutil.TestIOUtils;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.transport.htcli.UploadMetadataGenTransferDecorator;
import org.commonjava.maven.galley.transport.htcli.testutil.TestCacheProvider;
import org.commonjava.maven.galley.transport.htcli.testutil.TestFileEventManager;
import org.commonjava.maven.galley.transport.htcli.testutil.TestSpecialPathManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadMetadataGenTransferDecoratorTest
{
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private static final Integer FILE_SIZE = 2536;

    private static final String MOCK_TIME = "201801020231568";

    private final String tempRepo = "test-repo";

    private final Location loc =
            new SimpleLocation( tempRepo, "file:///" + tempRepo, true, false, true, true, false, false );

    private final TestSpecialPathManager specialPathManager = new TestSpecialPathManager();

    private final FileEventManager events = new TestFileEventManager();

    private File tempFolder;

    private CacheProvider provider;

    @Before
    public void prepare()
    {
        tempFolder = TestIOUtils.newTempFolder( folder, "cache" );
        final UploadMetadataGenTransferDecorator decorator =
                new UploadMetadataGenTransferDecorator( specialPathManager, null );
        provider = new TestCacheProvider( tempFolder, events, new TransferDecoratorManager( decorator ) );
    }

    @Test
    public void testRealArtifactWrite()
            throws Exception
    {
        final String path = "/path/to/test.jar";
        final String httpMetaPath = "/path/to/test.jar.http-metadata.json";
        transferWrite( path, httpMetaPath );
        assertFile( path, true, httpMetaPath, true );

        final String pomPath = "/path/to/test.pom";
        final String pomHttpMetaPath = "/path/to/test.pom.http-metadata.json";
        transferWrite( pomPath, pomHttpMetaPath );
        assertFile( pomPath, true, pomHttpMetaPath, true );
    }

    @Test
    public void testMetadataWrite()
            throws Exception
    {
        final String mavenMetaPath = "/path/to/maven-metadata.xml";
        final String mavenHttpMetaPath = "/path/to/maven-metadata.xml.http-metadata.json";
        transferWrite( mavenMetaPath, mavenHttpMetaPath );
        assertFile( mavenMetaPath, true, mavenHttpMetaPath, false );

        final String listMetaPath = "/path/to/.listing.txt";
        final String listHttpMetaPath = "/path/to/.listing.txt.xml.http-metadata.json";
        transferWrite( listMetaPath, listHttpMetaPath );
        assertFile( listMetaPath, true, listHttpMetaPath, false );
    }

    private void transferWrite( final String filePath, final String httpMetadataPath )
            throws Exception
    {
        final ConcreteResource resource = new ConcreteResource( loc, filePath );
        final Transfer transfer = provider.getTransfer( resource );
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put( "LAST-MODIFIED", Collections.singletonList( MOCK_TIME ) );
        headers.put( "CONTENT-LENGTH", Collections.singletonList( FILE_SIZE.toString() ) );
        final EventMetadata metadata = new EventMetadata().set( CacheProvider.STORE_HTTP_HEADERS, headers );
        final StringBuilder builder = new StringBuilder();

        for ( int i = 0; i < FILE_SIZE; i++ )
        {
            builder.append( i % 10 );
        }
        try (OutputStream stream = transfer.openOutputStream( TransferOperation.UPLOAD, true, metadata ))
        {
            stream.write( builder.toString().getBytes() );
        }

    }

    private void assertFile( final String filePath, boolean fileExists, final String httpMetaPath, boolean metaExists )
            throws Exception
    {
        final Path artifactPath = Paths.get( tempFolder.getAbsolutePath(), tempRepo, filePath );
        assertThat( Files.isRegularFile( artifactPath, LinkOption.NOFOLLOW_LINKS ), equalTo( fileExists ) );
        final Path metadataPath = Paths.get( tempFolder.getAbsolutePath(), tempRepo, httpMetaPath );
        assertThat( Files.isRegularFile( metadataPath, LinkOption.NOFOLLOW_LINKS ), equalTo( metaExists ) );
        if ( metaExists )
        {
            String metaContent = FileUtils.readFileToString( metadataPath.toFile(), Charset.defaultCharset() );
            assertThat( metaContent.contains( FILE_SIZE.toString() ), equalTo( true ) );
            assertThat( metaContent.contains( MOCK_TIME ), equalTo( true ) );
        }
    }
}
