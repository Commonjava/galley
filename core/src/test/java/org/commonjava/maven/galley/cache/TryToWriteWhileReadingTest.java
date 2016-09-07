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
