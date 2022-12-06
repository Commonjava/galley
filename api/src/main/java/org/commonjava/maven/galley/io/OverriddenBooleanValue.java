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
package org.commonjava.maven.galley.io;

public enum OverriddenBooleanValue
{

    OVERRIDE_TRUE( true, true ),
    OVERRIDE_FALSE( true, false ),
    DEFER( false, null );

    private final boolean overrides;

    private final Boolean result;


    OverriddenBooleanValue( final boolean overrides, final Boolean result )
    {
        this.overrides = overrides;
        this.result = result;
    }


    public boolean overrides()
    {
        return overrides;
    }

    public boolean getResult()
    {
        return result;
    }

}
