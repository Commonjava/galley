package org.commonjava.maven.galley.embed;

import org.commonjava.test.http.expect.ExpectationServer;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by jdcasey on 9/14/15.
 */
@RunWith( WeldJUnit4Runner.class )
public class EmbeddableCDIProducerStartupTest
        extends AbstractEmbeddableCDIProducerTest
{
    @Test
    public void startUp()
    {
        System.out.println( "Just making sure weld can start." );
    }

}
