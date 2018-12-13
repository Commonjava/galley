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
package org.commonjava.maven.galley.maven.internal.metadata;

import org.commonjava.atlas.maven.ident.ref.ProjectRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.spi.metadata.MetadataMapper;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;

import java.util.ArrayList;
import java.util.List;

import static org.commonjava.maven.galley.maven.util.ArtifactPathUtils.formatMetadataPath;

// TODO: Consider injection...@ApplicationScoped
public class StandardMetadataMapper
    implements MetadataMapper
{
    @Override
    public List<ConcreteResource> createResource( List<Location> locations, String f, ProjectRef projectRef,
                                                  String groupId ) throws TransferException
    {
        final List<ConcreteResource> results = new ArrayList<>();

        for ( Location location : locations )
        {
            results.add( createResource( location, f, projectRef, groupId ) );
        }

        return results;
    }

    @Override
    public ConcreteResource createResource( Location location, String f, ProjectRef projectRef, String groupId )
                    throws TransferException
    {
        final String fileName = establishFileName( f, location );
        final String p = projectRef == null ?
                        formatMetadataPath( groupId, fileName ) :
                        formatMetadataPath( projectRef, fileName );
        return new ConcreteResource( location, p );
    }

    protected String establishFileName( String fileName, Location location )
    {
        if ( fileName == null )
        {
            switch (location.getUri().substring( 0, 4 ) )
            {
                case "file":
                {
                    fileName = MetadataMapper.LOCAL_FILENAME;
                    break;
                }
                default:
                {
                    fileName = MetadataMapper.DEFAULT_FILENAME;
                    break;
                }
            }
        }
        return fileName;
    }
}
