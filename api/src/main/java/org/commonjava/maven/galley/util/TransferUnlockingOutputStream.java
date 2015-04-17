/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.Transfer.TransferUnlocker;

public class TransferUnlockingOutputStream
    extends FilterOutputStream
{

    private final TransferUnlocker unlocker;

    public TransferUnlockingOutputStream( final OutputStream out, final TransferUnlocker unlocker )
    {
        super( out );
        this.unlocker = unlocker;
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();
        unlocker.unlock();
    }

}
