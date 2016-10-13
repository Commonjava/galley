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
package org.commonjava.maven.galley.spi.io;

import org.commonjava.maven.galley.GalleyException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.SpecialPathMatcher;
import org.commonjava.maven.galley.model.Transfer;

/**
 * Created by jdcasey on 1/27/16.
 */
public interface SpecialPathManager
{

    void registerSpecialPathInfo( SpecialPathInfo pathInfo );

    void deregisterSpecialPathInfo( SpecialPathInfo pathInfo );

    SpecialPathInfo getSpecialPathInfo( ConcreteResource resource );

    SpecialPathInfo getSpecialPathInfo( Transfer transfer );

    @Deprecated
    SpecialPathInfo getSpecialPathInfo( Location location, String path );

    SpecialPathInfo getSpecialPathInfo( String path );
}
