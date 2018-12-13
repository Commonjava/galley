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
package org.commonjava.maven.galley.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.commonjava.maven.galley.event.FileStorageEvent;
import org.commonjava.maven.galley.model.Transfer.TransferUnlocker;
import org.commonjava.maven.galley.spi.event.FileEventManager;

public class TransferOutputStream
    extends FilterOutputStream
{

    private final TransferUnlocker unlocker;

    private final FileEventManager fileEventManager;

    private final FileStorageEvent event;

    public TransferOutputStream( final OutputStream out, final TransferUnlocker unlocker, final FileStorageEvent event,
                                 final FileEventManager fileEventManager )
    {
        super( out );
        this.unlocker = unlocker;
        this.event = event;
        this.fileEventManager = fileEventManager;
    }

    public TransferOutputStream( final OutputStream out, final TransferUnlocker unlocker )
    {
        super( out );
        this.unlocker = unlocker;
        this.event = null;
        this.fileEventManager = null;
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();
        unlocker.unlock();

        if ( fileEventManager != null )
        {
            fileEventManager.fire( event );
        }
    }

}
