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
package org.commonjava.maven.galley.event;

import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

public class FileStorageEvent
    extends FileEvent
{

    final TransferOperation type;

    public FileStorageEvent( final TransferOperation type, final Transfer transfer, final EventMetadata eventMetadata )
    {
        super( transfer, eventMetadata );
        this.type = type;
    }

    public TransferOperation getType()
    {
        return type;
    }

    @Override
    public String getExtraInfo()
    {
        return "type=" + type.name();
    }

}
