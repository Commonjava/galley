package org.commonjava.maven.galley.maven.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.internal.type.StandardTypeMapper;
import org.commonjava.maven.galley.maven.spi.type.TypeMapper;
import org.junit.Test;

public class ArtifactPathUtilsTest
{

    @Test
    public void handleRemoteSnapshotArtifactPath()
        throws Exception
    {
        final ProjectVersionRef pvr = new ProjectVersionRef( "org.group", "artifact-id", "1.0-20140603.151226-11" );
        final TypeMapper mapper = new StandardTypeMapper();

        final String path = ArtifactPathUtils.formatArtifactPath( pvr.asJarArtifact(), mapper );

        assertThat( path.equals( "org/group/artifact-id/1.0-SNAPSHOT/artifact-id-" + pvr.getVersionString() + ".jar" ),
                    equalTo( true ) );
    }

}
