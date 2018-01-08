/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley.maven.model.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Node;

public class MavenXmlMixin<T>
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
        this.subPaths = new HashSet<>( Arrays.asList( pathPatterns ) );
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

    public String resolveXPathToRawString( final String path )
        throws GalleyMavenException
    {
        return mixin.resolveXPathToRawString( path, true, -1 );
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
