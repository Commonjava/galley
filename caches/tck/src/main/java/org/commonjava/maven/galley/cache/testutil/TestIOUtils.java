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
package org.commonjava.maven.galley.cache.testutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class TestIOUtils
{
    public static String readFromStream( InputStream in )
            throws IOException
    {
        if ( in == null )
        {
            System.out.println( "Can not read content as the input stream is null." );
            return null;
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read;
        final byte[] buf = new byte[512];
        while ( ( read = in.read( buf ) ) > -1 )
        {
            baos.write( buf, 0, read );
        }

        String readingResult = new String( baos.toByteArray(), "UTF-8" );
        baos.close();
        in.close();

        return readingResult;
    }

    public static boolean latchWait( CountDownLatch latch, long timeout, TimeUnit unit )
    {
        try
        {
            return latch.await( timeout, unit );
        }
        catch ( InterruptedException e )
        {
            System.out.println( "Threads await Exception." );
            return false;
        }
    }

}
