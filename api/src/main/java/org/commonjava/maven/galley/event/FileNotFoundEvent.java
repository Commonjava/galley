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

import org.commonjava.maven.galley.model.Resource;
import org.slf4j.MDC;

import java.util.Map;

public class FileNotFoundEvent
{

    private final Resource resource;

    private final EventMetadata eventMetadata;

    private final Map<String, String> contextMap;

    public FileNotFoundEvent( final Resource resource, final EventMetadata eventMetadata )
    {
        this.resource = resource;
        this.eventMetadata = eventMetadata;
        MDC.put( FileEvent.CREATION_THREAD_NAME, Thread.currentThread().getName() );
        //noinspection unchecked
        contextMap = MDC.getCopyOfContextMap();
    }

    public EventMetadata getEventMetadata()
    {
        return eventMetadata;
    }

    public Resource getResource()
    {
        return resource;
    }

    public Map<String, String> getMDCMap()
    {
        return contextMap;
    }

}
