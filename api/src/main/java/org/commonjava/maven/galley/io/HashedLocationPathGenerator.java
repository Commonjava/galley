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
package org.commonjava.maven.galley.io;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.apache.commons.codec.digest.DigestUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.util.PathUtils;

@Named
@Alternative
public class HashedLocationPathGenerator
    implements PathGenerator
{

    @Override
    public String getFilePath( final ConcreteResource resource )
    {
        return PathUtils.normalize( formatLocationDir( resource.getLocation() ), resource.getPath() );
    }

    private String formatLocationDir( final Location loc )
    {
        return DigestUtils.sha1Hex( loc.getUri() );
    }

}
