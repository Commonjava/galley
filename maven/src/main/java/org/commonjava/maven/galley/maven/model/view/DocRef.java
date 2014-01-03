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
