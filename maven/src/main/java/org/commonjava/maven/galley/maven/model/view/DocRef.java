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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.parse.JXPathUtils;
import org.w3c.dom.Document;

public final class DocRef<T extends ProjectRef>
{

    private final Document doc;

    private JXPathContext docContext;

    private final T ref;

    private final Object source;

    private final Map<String, Object> attributes = new HashMap<String, Object>();

    public DocRef( final T ref, final Object source, final Document doc )
    {
        this.ref = ref;
        this.source = source;
        this.doc = doc;

        // ugly, but prevents need to prefix all xpath segments...
        this.doc.getDocumentElement()
                .removeAttribute( "xmlns" );
    }

    public Document getDoc()
    {
        return doc;
    }

    public JXPathContext getDocContext()
    {
        if ( docContext == null )
        {
            docContext = JXPathUtils.newContext( doc );
        }

        return docContext;
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
