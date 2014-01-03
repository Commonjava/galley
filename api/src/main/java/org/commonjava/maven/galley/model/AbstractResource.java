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
package org.commonjava.maven.galley.model;

import static org.commonjava.maven.galley.util.PathUtils.ROOT;
import static org.commonjava.maven.galley.util.PathUtils.normalize;
import static org.commonjava.maven.galley.util.PathUtils.parentPath;

public abstract class AbstractResource
    implements Resource
{

    private final String path;

    protected abstract Resource newDerivedResource( String... path );

    public AbstractResource()
    {
        path = ROOT;
    }

    protected AbstractResource( final String... path )
    {
        this.path = normalize( path );
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public boolean isRoot()
    {
        return path == ROOT || ROOT.equals( path );
    }

    @Override
    public Resource getParent()
    {
        if ( isRoot() )
        {
            return null;
        }

        return newDerivedResource( parentPath( path ) );
    }

    @Override
    public Resource getChild( final String file )
    {
        return newDerivedResource( path, file );
    }

}
