package org.commonjava.maven.galley;
import org.junit.Test;

import static org.junit.Assert.fail;

public class FailedTest
{

    @Test
    public void test()
    {
        fail("Hi, I failed!");
    }

}
