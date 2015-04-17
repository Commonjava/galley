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
package org.commonjava.maven.galley.spi.nfc;

import java.util.Map;
import java.util.Set;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.ConcreteResource;

public interface NotFoundCache
{

    void addMissing( ConcreteResource resource );

    boolean isMissing( ConcreteResource resource );

    void clearMissing( Location location );

    void clearMissing( ConcreteResource resource );

    void clearAllMissing();

    Map<Location, Set<String>> getAllMissing();

    Set<String> getMissing( Location location );

}
