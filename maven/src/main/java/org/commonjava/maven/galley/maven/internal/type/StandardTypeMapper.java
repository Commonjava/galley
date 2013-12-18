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
