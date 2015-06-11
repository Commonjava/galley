package org.commonjava.maven.galley.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EventMetadata
    implements Iterable<Entry<Object, Object>>
{

    private final Map<Object, Object> metadata = new HashMap<Object, Object>();

    public Map<Object, Object> getMetadata()
    {
        return metadata;
    }

    public EventMetadata set( final Object key, final Object value )
    {
        metadata.put( key, value );
        return this;
    }

    public Object get( final Object key )
    {
        return metadata.get( key );
    }

    public boolean containsKey( final Object key )
    {
        return metadata.containsKey( key );
    }

    public boolean containsValue( final Object value )
    {
        return metadata.containsValue( value );
    }

    @Override
    public Iterator<Entry<Object, Object>> iterator()
    {
        return metadata.entrySet()
                       .iterator();
    }

}
