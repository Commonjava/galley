/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

public class PropertiesView
    extends MavenGAVView
{
    public PropertiesView( final MavenPomView pomView, final Element element, OriginInfo originInfo )
    {
        super( pomView, element, originInfo );
    }

    /**
     * Return any key/value properties from within this view.
     * @return Properties - containing key/values from this view.
     */
    public Properties getProperties()
    {
        List<Element> elements = getElements( "*" );
        Properties result = new Properties();

        if ( elements != null )
        {
            for ( Element e : elements )
            {
                Node value = e.getFirstChild();
                result.setProperty( e.getNodeName(), ( value == null ?
                        "" :
                        getPomView().resolveExpressions( e.getFirstChild().getNodeValue() ) ) );
            }
        }
        return result;
    }

    /**
     * Utility function to aggregate a list of PropertiesViews (i.e. inherited poms) and to create
     * a merged Properties.
     *
     * @param lpv
     * @return
     */
    public static Properties aggregateProperties (List<PropertiesView> lpv)
    {
        Properties result = new Properties();
        ListIterator<PropertiesView> iterator = lpv.listIterator(lpv.size());

        // Iterate in reverse order so children can override parent.
        while ( iterator.hasPrevious() )
        {
            Properties p = iterator.previous().getProperties();

            for ( String key : p.stringPropertyNames())
            {
                if ( ! result.containsKey( key ) )
                {
                    result.setProperty( key, p.getProperty( key ) );
                }
                else if ( p.getProperty( key ).length() > 0 && ! p.getProperty( key ).startsWith( "${" ))
                {
                    // The result already contains this key. We only prefer new key/value
                    // if its 'better' i.e. not a property.
                    result.setProperty( key, p.getProperty( key ) );
                }
            }
        }

        return result;
    }
}
