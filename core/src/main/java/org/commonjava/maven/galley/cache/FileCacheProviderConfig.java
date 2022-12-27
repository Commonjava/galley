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
package org.commonjava.maven.galley.cache;

import java.io.File;

public class FileCacheProviderConfig
{
    private Boolean aliasLinking;

    private final File cacheBasedir;

    public FileCacheProviderConfig( final File cacheBasedir )
    {
        this.cacheBasedir = cacheBasedir;
    }

    public FileCacheProviderConfig withAliasLinking( final boolean aliasLinking )
    {
        this.aliasLinking = aliasLinking;
        return this;
    }

    public boolean isAliasLinking()
    {
        return aliasLinking == null || aliasLinking;
    }

    public File getCacheBasedir()
    {
        return cacheBasedir;
    }

}
