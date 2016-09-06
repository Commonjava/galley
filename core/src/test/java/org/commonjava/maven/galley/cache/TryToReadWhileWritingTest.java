package org.commonjava.maven.galley.cache;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@BMUnitConfig( loadDirectory = "target/test-classes/bmunit", debug = true )
@BMScript( "TryToReadWhileWritingTestCase.btm" )
public class TryToReadWhileWritingTest
        extends AbstractFileCacheBMUnitTest
{
    @Test
    public void run()
    {
        new Thread( new WriteThread() ).start();
        new Thread( new ReadThread() ).start();
        try
        {
            latch.await();
        }
        catch ( Exception e )
        {
            System.out.println( "Threads await Exception." );
        }
        assertThat( result, equalTo( content ) );
    }
}
