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
package org.commonjava.maven.galley.cache.partyline;

import java.io.File;

public class PartyLineCacheProviderConfig
{
    private static final int DEFAULT_TIMEOUT_SECONDS = 86400;

    private Boolean aliasLinking;

    private final File cacheBasedir;

    private Boolean timeoutProcessing;

    private Integer defaultTimeoutSeconds;

    public PartyLineCacheProviderConfig( final File cacheBasedir )
    {
        this.cacheBasedir = cacheBasedir;
    }

    public PartyLineCacheProviderConfig withAliasLinkingEnabled( final boolean aliasLinking )
    {
        this.aliasLinking = aliasLinking;
        return this;
    }

    public boolean isAliasLinkingEnabled()
    {
        return aliasLinking == null || aliasLinking;
    }

    public PartyLineCacheProviderConfig withTimeoutProcessingEnabled( final boolean timeoutProcessing )
    {
        this.timeoutProcessing = timeoutProcessing;
        return this;
    }

    public boolean isTimeoutProcessingEnabled()
    {
        return timeoutProcessing != null && timeoutProcessing;
    }

    public File getCacheBasedir()
    {
        return cacheBasedir;
    }

    public PartyLineCacheProviderConfig withDefaultTimeoutSeconds( final int defaultTimeoutSeconds )
    {
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        return this;
    }

    public int getDefaultTimeoutSeconds()
    {
        return defaultTimeoutSeconds == null ? DEFAULT_TIMEOUT_SECONDS : defaultTimeoutSeconds;
    }

}
