/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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
