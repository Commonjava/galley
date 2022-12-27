/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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

/**
 * Created by jdcasey on 11/9/15.
 */
public class OriginInfo
{

    private boolean inherited;

    private boolean mixin;

    public OriginInfo(){}

    public OriginInfo( boolean inherited )
    {
        this.inherited = inherited;
    }

    public OriginInfo( OriginInfo info )
    {
        this.inherited = info.isInherited();
        this.mixin = info.isMixin();
    }

    public boolean isInherited()
    {
        return inherited;
    }

    public OriginInfo setInherited( boolean inherited )
    {
        this.inherited = inherited;
        return this;
    }

    public boolean isMixin()
    {
        return mixin;
    }

    public OriginInfo setMixin( boolean mixin )
    {
        this.mixin = mixin;
        return this;
    }
}
