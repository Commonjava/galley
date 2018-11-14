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

import org.commonjava.atlas.maven.ident.ref.TypeAndClassifier;

public class TypeMapping
{

    private final String extension;

    private final String classifier;

    public TypeMapping( final String extension, final String classifer )
    {
        this.extension = extension;
        this.classifier = classifer;
    }

    public TypeMapping( final String extension )
    {
        this( extension, null );
    }

    public TypeMapping( final TypeAndClassifier tc )
    {
        this( tc.getType(), tc.getClassifier() );
    }

    public TypeMapping overrideClassifier( final String classifier )
    {
        return new TypeMapping( extension, classifier );
    }

    public TypeMapping overrideClassifier( final TypeAndClassifier tc )
    {
        final String tcls = tc.getClassifier();
        if ( ( classifier == null && tcls != null ) || ( classifier != null && !classifier.equals( tcls ) ) )
        {
            return new TypeMapping( extension, tcls );
        }

        return this;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getClassifier()
    {
        return classifier;
    }

}
