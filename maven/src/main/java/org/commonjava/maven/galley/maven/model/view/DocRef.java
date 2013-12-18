package org.commonjava.maven.galley.maven.model.view;

import java.util.HashMap;
import java.util.Map;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.w3c.dom.Document;

public final class DocRef<T extends ProjectRef>
{

    private final Document doc;

    private final T ref;

    private final Object source;

    private final Map<String, Object> attributes = new HashMap<String, Object>();

    public DocRef( final T ref, final Object source, final Document doc )
    {
        this.ref = ref;
        this.source = source;
        this.doc = doc;
    }

    public Document getDoc()
    {
        return doc;
    }

    public T getRef()
    {
        return ref;
    }

    public Object getSource()
    {
        return source;
    }

    public void setAttribute( final String key, final Object value )
    {
        attributes.put( key, value );
    }

    public <C> C getAttribute( final String key, final Class<C> type )
    {
        final Object val = attributes.get( key );
        return val == null ? null : type.cast( val );
    }

    @Override
    public String toString()
    {
        return String.format( "DocRef [%s] (from: %s)", ref, source );
    }

}
