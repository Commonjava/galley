package org.commonjava.maven.galley.cache;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Test;

import static org.junit.Assert.assertNull;

@BMUnitConfig( loadDirectory = "target/test-classes/bmunit", debug = true )
@BMScript( "TryToWriteWhileReadingTestCase.btm" )
public class TryToWriteWhileReadingTest
        extends AbstractFileCacheBMUnitTest
{
    @Test
    public void run()
    {
        new Thread( new ReadThread() ).start();
        new Thread( new WriteThread() ).start();
        try
        {
            latch.await();
        }
        catch ( Exception e )
        {
            System.out.println( "Threads await Exception." );
        }
        assertNull( result );
    }
}
