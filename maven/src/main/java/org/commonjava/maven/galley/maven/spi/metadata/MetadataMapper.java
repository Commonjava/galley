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
package org.commonjava.maven.galley.maven.spi.metadata;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;

import java.util.List;

public interface MetadataMapper
{
    String DEFAULT_FILENAME = "maven-metadata.xml";
    String LOCAL_FILENAME = "maven-metadata-local.xml";

    List<ConcreteResource> createResource( List<Location> location, String fileName, ProjectRef projectRef, String groupId)
                    throws TransferException;

    ConcreteResource createResource( Location location, String fileName, ProjectRef projectRef, String groupId)
                    throws TransferException;
}
