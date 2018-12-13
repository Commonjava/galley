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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.TransferBatch;
import org.commonjava.maven.galley.model.VirtualResource;

// it only really makes sense to use this in a retrieveFirst() type scenario...
public class ArtifactBatch
    extends TransferBatch
    implements Iterable<ArtifactRef>
{

    private final Map<ArtifactRef, List<? extends Location>> artifacts;

    private Map<ArtifactRef, List<ConcreteResource>> artifactMappings;

    public ArtifactBatch( final List<? extends Location> locations, final Collection<ArtifactRef> artifacts )
    {
        this.artifacts = new HashMap<>();
        for ( final ArtifactRef artifact : artifacts )
        {
            this.artifacts.put( artifact, locations );
        }
    }

    public ArtifactBatch( final Map<ArtifactRef, List<? extends Location>> artifacts )
    {
        this.artifacts = artifacts;
    }

    public void setArtifactToResourceMapping( final Map<ArtifactRef, Resource> mappings )
    {
        artifactMappings = new HashMap<>();
        final Set<ConcreteResource> resources = new HashSet<>();
        for ( final Entry<ArtifactRef, Resource> entry : mappings.entrySet() )
        {
            final ArtifactRef artifact = entry.getKey();
            final Resource resource = entry.getValue();

            if ( resource instanceof ConcreteResource )
            {
                artifactMappings.put( artifact, Collections.singletonList( (ConcreteResource) resource ) );
                resources.add( (ConcreteResource) resource );
            }
            else
            {
                final List<ConcreteResource> res = ( (VirtualResource) resource ).toConcreteResources();
                artifactMappings.put( artifact, res );
                resources.addAll( res );
            }
        }

        setResources( resources );
    }

    public Map<ArtifactRef, List<ConcreteResource>> getArtifactToConcreteResourceMapping()
    {
        return artifactMappings;
    }

    public List<? extends Location> getLocations( final ArtifactRef ref )
    {
        return artifacts.get( ref );
    }

    public Set<ArtifactRef> getArtifactRefs()
    {
        return artifacts.keySet();
    }

    @Override
    public Iterator<ArtifactRef> iterator()
    {
        return artifacts.keySet()
                        .iterator();
    }

    public int size()
    {
        return artifacts.size();
    }

}
