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
package org.commonjava.maven.galley.maven.model;

import java.util.Map.Entry;

import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.atlas.maven.ident.version.SingleVersion;
import org.commonjava.maven.galley.model.Location;

public final class ProjectVersionRefLocation
{

    private final Location location;

    private final ProjectVersionRef ref;

    public ProjectVersionRefLocation( final ProjectVersionRef ref, final Location location )
    {
        this.ref = ref;
        this.location = location;
    }

    public ProjectVersionRefLocation( final ProjectVersionRef original, final Entry<SingleVersion, Location> entry )
    {
        this.ref = original.selectVersion( entry.getKey() );
        this.location = entry.getValue();
    }

    public Location getLocation()
    {
        return location;
    }

    public ProjectVersionRef getRef()
    {
        return ref;
    }

}
