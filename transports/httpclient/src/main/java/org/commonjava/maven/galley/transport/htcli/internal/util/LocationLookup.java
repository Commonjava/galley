/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
package org.commonjava.maven.galley.transport.htcli.internal.util;

import org.commonjava.maven.galley.model.Location;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jdcasey on 11/5/15.
 */
public class LocationLookup
{
    private final Map<String, WeakReference<Location>> locations = new HashMap<>();

    public void register( Location location )
    {
        locations.put( location.getName(), new WeakReference<>( location ) );
    }

    public void deregister( Location location )
    {
        locations.remove( location.getName() );
    }

    public Location lookup( String name )
    {
        WeakReference<Location> ref = locations.get( name );
        return ref == null ? null : ref.get();
    }
}
