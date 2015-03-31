/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.cache.partyline;

import java.io.File;

import javax.inject.Named;

@Named
public class PartyLineCacheProviderConfig
{
    private Boolean aliasLinking;

    private final File cacheBasedir;

    public PartyLineCacheProviderConfig( final File cacheBasedir )
    {
        this.cacheBasedir = cacheBasedir;
    }

    public PartyLineCacheProviderConfig withAliasLinking( final boolean aliasLinking )
    {
        this.aliasLinking = aliasLinking;
        return this;
    }

    public boolean isAliasLinking()
    {
        return aliasLinking == null ? true : aliasLinking;
    }

    public File getCacheBasedir()
    {
        return cacheBasedir;
    }

}
