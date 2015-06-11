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
package org.commonjava.maven.galley.event;

import org.commonjava.maven.galley.model.Transfer;

public class FileEvent
{

    private final Transfer transfer;

    private final EventMetadata eventMetadata;

    protected FileEvent( final Transfer transfer, final EventMetadata eventMetadata )
    {
        this.transfer = transfer;
        this.eventMetadata = eventMetadata;
    }

    public EventMetadata getEventMetadata()
    {
        return eventMetadata;
    }

    public Transfer getTransfer()
    {
        return transfer;
    }

    public String getExtraInfo()
    {
        return "";
    }

    @Override
    public String toString()
    {
        return String.format( "%s [extra-info=%s, transfer=%s]", getClass().getSimpleName(), getExtraInfo(), transfer );
    }

}
