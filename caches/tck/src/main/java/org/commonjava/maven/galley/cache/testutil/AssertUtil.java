package org.commonjava.maven.galley.cache.testutil;

import java.io.IOException;

import static org.junit.Assert.fail;

public class AssertUtil
{
    @FunctionalInterface
    public interface CheckedFunction
    {
        void apply() throws IOException;
    }

    public static void assertThrows( Class exceptionClass, CheckedFunction o )
    {
        try
        {
            o.apply();
            fail();
        }
        catch ( Exception e )
        {
            if ( exceptionClass.isInstance( e ) )
            {
                System.out.println( "Expected: " + e.getMessage() ); // ok
            }
            else
            {
                fail( "Expected: " + exceptionClass.getName() + ", but got " + e.getClass().getName() );
            }
        }
    }

}
