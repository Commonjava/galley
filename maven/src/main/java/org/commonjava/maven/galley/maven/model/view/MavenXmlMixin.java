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
package org.commonjava.maven.galley.maven.model.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Node;

public class MavenXmlMixin<T extends ProjectRef>
{

    public static final String[] DEPENDENCY_MIXIN = { "dependencyManagement/dependencies/dependency", "dependencyManagement//dependency" };

    private final Set<String> subPaths;

    private final MavenXmlView<T> mixin;

    public MavenXmlMixin( final MavenXmlView<T> mixin, final Set<String> pathPatterns )
    {
        this.mixin = mixin;
        this.subPaths = pathPatterns;
    }

    public MavenXmlMixin( final MavenXmlView<T> mixin, final String... pathPatterns )
    {
        this.mixin = mixin;
        this.subPaths = new HashSet<String>( Arrays.asList( pathPatterns ) );
    }

    public boolean matches( final String path )
    {
        for ( final String pattern : subPaths )
        {
            if ( path.contains( pattern ) )
            {
                return true;
            }
        }

        return false;
    }

    public Set<String> getSubPaths()
    {
        return subPaths;
    }

    public MavenXmlView<T> getMixin()
    {
        return mixin;
    }

    public String resolveXPathExpression( final String path )
        throws GalleyMavenException
    {
        return mixin.resolveXPathExpression( path, true, -1 );
    }

    public Node resolveXPathToNode( final String path )
        throws GalleyMavenException
    {
        return mixin.resolveXPathToNode( path, true, -1 );
    }

    @Override
    public String toString()
    {
        return "Mixin [ref: " + mixin.getRef() + ", paths: " + subPaths + "]";
    }
}
