/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
