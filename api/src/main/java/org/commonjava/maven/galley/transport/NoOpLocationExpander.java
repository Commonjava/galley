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
package org.commonjava.maven.galley.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.VirtualResource;
import org.commonjava.maven.galley.spi.transport.LocationExpander;

@Named( "no-op-galley-location-expander" )
@Alternative
public class NoOpLocationExpander
    implements LocationExpander
{

    @SuppressWarnings( "unchecked" )
    @Override
    public <T extends Location> List<Location> expand( final Collection<T> locations )
        throws TransferException
    {
        return locations instanceof List ? (List<Location>) locations : new ArrayList<Location>( locations );
    }

    @Override
    public List<Location> expand( final Location... locations )
        throws TransferException
    {
        return Arrays.asList( locations );
    }

    @Override
    public VirtualResource expand( final Resource resource )
    {
        if ( resource instanceof VirtualResource )
        {
            return (VirtualResource) resource;
        }
        else
        {
            return new VirtualResource( (ConcreteResource) resource );
        }
    }

}
