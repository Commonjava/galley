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
package org.commonjava.maven.galley.io;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.apache.commons.codec.digest.DigestUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.util.PathUtils;

@Named( "hashed-location-galley-pathgen" )
@Alternative
public class HashedLocationPathGenerator
    implements PathGenerator
{

    @Override
    public String getFilePath( final ConcreteResource resource )
    {
        return PathUtils.normalize( formatLocationDir( resource.getLocation() ), resource.getPath() )
                        .toString();
    }

    private String formatLocationDir( final Location loc )
    {
        return DigestUtils.shaHex( loc.getUri() );
    }

}
