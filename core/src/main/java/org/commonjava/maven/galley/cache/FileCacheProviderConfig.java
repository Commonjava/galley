/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.cache;

import java.io.File;

import javax.inject.Named;

@Named
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
        return aliasLinking == null ? true : aliasLinking;
    }

    public File getCacheBasedir()
    {
        return cacheBasedir;
    }

}
