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

import org.w3c.dom.Node;

/**
 * Created by jdcasey on 11/9/15.
 */
public class XmlNodeInfo
{
    private boolean inherited;
    private boolean mixin;

    private Node node;

    public XmlNodeInfo( boolean inherited, boolean mixin, Node node )
    {
        this.inherited = inherited;
        this.mixin = mixin;
        this.node = node;
    }

    public XmlNodeInfo( boolean inherited, Node node )
    {
        this( inherited, false, node );
    }

    public boolean isInherited()
    {
        return inherited;
    }

    public void setInherited( boolean inherited )
    {
        this.inherited = inherited;
    }

    public boolean isMixin()
    {
        return mixin;
    }

    public void setMixin( boolean mixin )
    {
        this.mixin = mixin;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode( Node node )
    {
        this.node = node;
    }

    public OriginInfo getOriginInfo()
    {
        return new OriginInfo( inherited ).setMixin( mixin );
    }
}
