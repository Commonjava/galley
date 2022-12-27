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
package org.commonjava.maven.galley.cache.pathmapped;

import java.io.File;

public class PathMappedCacheProviderConfig
{
    public static final String DEFAULT_STORAGE_STRATEGY = "default";

    private final File cacheBasedir; // e.g., storage/

    private String storageStrategy = DEFAULT_STORAGE_STRATEGY;

    private static final int DEFAULT_TIMEOUT_SECONDS = -1; // never timeout

    private boolean timeoutProcessingEnabled;

    private int defaultTimeoutSeconds = DEFAULT_TIMEOUT_SECONDS;

    public PathMappedCacheProviderConfig( final File cacheBasedir )
    {
        this.cacheBasedir = cacheBasedir;
    }

    public File getCacheBasedir()
    {
        return cacheBasedir;
    }

    public PathMappedCacheProviderConfig withStorageStrategy( String storageStrategy )
    {
        this.storageStrategy = storageStrategy;
        return this;
    }

    public String getStorageStrategy()
    {
        return storageStrategy;
    }

    public PathMappedCacheProviderConfig withDefaultTimeoutSeconds( final int defaultTimeoutSeconds )
    {
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        return this;
    }

    public int getDefaultTimeoutSeconds()
    {
        return defaultTimeoutSeconds;
    }

    public PathMappedCacheProviderConfig withTimeoutProcessingEnabled( final boolean timeoutProcessingEnabled )
    {
        this.timeoutProcessingEnabled = timeoutProcessingEnabled;
        return this;
    }

    public boolean isTimeoutProcessingEnabled()
    {
        return timeoutProcessingEnabled;
    }

}
