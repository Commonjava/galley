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
package org.commonjava.maven.galley.model;

import java.io.File;

/**
 * Created by jdcasey on 1/27/16.
 */
public class FilePatternMatcher
    implements SpecialPathMatcher
{
    private final String pattern;

    public FilePatternMatcher( String pattern )
    {
        this.pattern = pattern;
    }

    @Override
    public boolean matches( Location location, String path )
    {
        return path == null ? false : new File( path).getName().matches( pattern );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof FilePatternMatcher ) )
        {
            return false;
        }

        FilePatternMatcher that = (FilePatternMatcher) o;

        return pattern.equals( that.pattern );

    }

    @Override
    public int hashCode()
    {
        return pattern.hashCode();
    }
}
