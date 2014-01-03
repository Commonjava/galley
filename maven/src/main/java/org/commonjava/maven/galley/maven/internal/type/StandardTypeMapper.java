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
package org.commonjava.maven.galley.maven.internal.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;
import org.commonjava.maven.galley.maven.spi.type.TypeMapper;
import org.commonjava.maven.galley.model.TypeMapping;

@ApplicationScoped
public class StandardTypeMapper
    implements TypeMapper
{

    private final Map<String, TypeMapping> mappings = Collections.unmodifiableMap( new HashMap<String, TypeMapping>()
    {
        private static final long serialVersionUID = 1L;

        {
            put( "ejb", new TypeMapping( "jar" ) );
            put( "ejb-client", new TypeMapping( "jar", "client" ) );
            put( "test-jar", new TypeMapping( "jar", "tests" ) );
            put( "maven-plugin", new TypeMapping( "jar" ) );
            put( "java-source", new TypeMapping( "jar", "sources" ) );
            put( "javadoc", new TypeMapping( "jar", "javadoc" ) );
        }

    } );

    @Override
    public TypeMapping lookup( final TypeAndClassifier tc )
    {
        if ( tc == null )
        {
            return null;
        }

        TypeMapping mapping = mappings.get( tc.getType() );
        if ( mapping == null )
        {
            mapping = new TypeMapping( tc );
        }
        else
        {
            mapping = mapping.overrideClassifier( tc );
        }

        return mapping;
    }

    @Override
    public Map<String, TypeMapping> getMappings()
    {
        return mappings;
    }

}
