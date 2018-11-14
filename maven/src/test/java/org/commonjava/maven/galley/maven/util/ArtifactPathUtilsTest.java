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
package org.commonjava.maven.galley.maven.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.atlas.maven.ident.ref.SimpleProjectVersionRef;
import org.commonjava.maven.galley.maven.internal.type.StandardTypeMapper;
import org.commonjava.maven.galley.maven.spi.type.TypeMapper;
import org.junit.Test;

public class ArtifactPathUtilsTest
{

    @Test
    public void handleRemoteSnapshotArtifactPath()
        throws Exception
    {
        final ProjectVersionRef pvr = new SimpleProjectVersionRef( "org.group", "artifact-id", "1.0-20140603.151226-11" );
        final TypeMapper mapper = new StandardTypeMapper();

        final String path = ArtifactPathUtils.formatArtifactPath( pvr.asJarArtifact(), mapper );

        assertThat( path.equals( "org/group/artifact-id/1.0-SNAPSHOT/artifact-id-" + pvr.getVersionString() + ".jar" ),
                    equalTo( true ) );
    }

}
