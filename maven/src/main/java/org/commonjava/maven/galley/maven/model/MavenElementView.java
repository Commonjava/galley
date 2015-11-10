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
package org.commonjava.maven.galley.maven.model;

import org.commonjava.maven.galley.maven.model.view.MavenPomElementView;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.model.view.OriginInfo;
import org.w3c.dom.Element;

@Deprecated
public class MavenElementView
    extends MavenPomElementView
{

    public MavenElementView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element, new OriginInfo(), managementXpathFragment );
    }

    public MavenElementView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element, new OriginInfo() );
    }

}
