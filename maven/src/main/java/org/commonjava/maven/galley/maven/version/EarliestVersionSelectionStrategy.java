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
package org.commonjava.maven.galley.maven.version;

import java.util.Collections;
import java.util.List;

import org.commonjava.atlas.maven.ident.version.SingleVersion;

@SuppressWarnings( "unused" )
public final class EarliestVersionSelectionStrategy
    implements VersionSelectionStrategy
{

    public static EarliestVersionSelectionStrategy INSTANCE = new EarliestVersionSelectionStrategy();

    private EarliestVersionSelectionStrategy()
    {
    }

    @Override
    public SingleVersion select( final List<SingleVersion> candidates )
    {
        if ( candidates.isEmpty() )
        {
            return null;
        }

        Collections.sort( candidates );
        return candidates.get( 0 );
    }

}
