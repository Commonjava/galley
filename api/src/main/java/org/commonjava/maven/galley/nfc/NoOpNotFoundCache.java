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
package org.commonjava.maven.galley.nfc;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;

@Named( "no-op-galley-nfc" )
@Alternative
public class NoOpNotFoundCache
    implements NotFoundCache
{

    @Override
    public void addMissing( final ConcreteResource resource )
    {
    }

    @Override
    public boolean isMissing( final ConcreteResource resource )
    {
        return false;
    }

    @Override
    public void clearMissing( final Location location )
    {
    }

    @Override
    public void clearMissing( final ConcreteResource resource )
    {
    }

    @Override
    public void clearAllMissing()
    {
    }

    @Override
    public Map<Location, Set<String>> getAllMissing()
    {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getMissing( final Location location )
    {
        return Collections.emptySet();
    }

}
