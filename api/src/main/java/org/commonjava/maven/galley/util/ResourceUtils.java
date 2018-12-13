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

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.ConcreteResource;

public final class ResourceUtils
{
    public ResourceUtils()
    {
    }

    public static ConcreteResource storageResource ( final ConcreteResource resource, final EventMetadata eventMetadata)
    {
        String storagePath = PathUtils.storagePath( resource.getPath(), eventMetadata );
        return new ConcreteResource( resource.getLocation(), storagePath );
    }
}
