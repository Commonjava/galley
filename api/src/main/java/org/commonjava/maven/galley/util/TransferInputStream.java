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
package org.commonjava.maven.galley.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.commonjava.maven.galley.event.FileAccessEvent;
import org.commonjava.maven.galley.spi.event.FileEventManager;

public class TransferInputStream
    extends FilterInputStream
{

    private final FileAccessEvent event;

    private final FileEventManager fileEventManager;

    public TransferInputStream( final InputStream in, final FileAccessEvent event,
                                final FileEventManager fileEventManager )
    {
        super( in );
        this.event = event;
        this.fileEventManager = fileEventManager;
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();
        fileEventManager.fire( event );
    }

}
