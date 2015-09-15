package org.commonjava.maven.galley.embed;

import org.apache.commons.io.IOUtils;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.commonjava.maven.galley.TransferManager;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.test.http.expect.ExpectationServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by jdcasey on 9/14/15.
 */
@RunWith(WeldJUnit4Runner.class)
public class EmbeddableCDI_HTTPArtifactDownload_Test
        extends AbstractEmbeddableCDIProducerTest
{
    private ExpectationServer server = new ExpectationServer();

    @Inject
    private ArtifactManager transfers;

    @Before
    public void before()
            throws IOException
    {
        server.start();
    }

    @After
    public void after()
    {
        if ( server != null )
        {
            server.stop();
        }
    }

    @Test
    public void resolveArtifactViaHttp()
            throws Exception
    {
        String path = "/group/artifact/1/artifact-1.pom";
        String content = "this is a test.";

        server.expect( path, 200, content );

        Transfer transfer =
                transfers.retrieve( new SimpleLocation( server.getBaseUri() ), new SimpleProjectVersionRef( "group", "artifact", "1" ).asPomArtifact() );

        assertThat( transfer, notNullValue() );

        InputStream stream = null;
        try
        {
            stream = transfer.openInputStream();
            assertThat( IOUtils.toString( stream ), equalTo( content ) );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }
    }

}
