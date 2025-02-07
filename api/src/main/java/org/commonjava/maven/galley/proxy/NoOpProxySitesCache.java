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
package org.commonjava.maven.galley.proxy;

import org.commonjava.maven.galley.spi.proxy.ProxySitesCache;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import java.util.Collections;
import java.util.Set;

@Named
@Alternative
public class NoOpProxySitesCache
        implements ProxySitesCache
{
    @Override
    public Set<String> getProxySites()
    {
        return Collections.emptySet();
    }

    @Override
    public boolean isProxySite( String site )
    {
        return false;
    }

    @Override
    public void saveProxySite( String site )
    {

    }

    @Override
    public void deleteProxySite( String site )
    {

    }

    @Override
    public void deleteAllProxySites()
    {

    }
}
