/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.model;

import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;

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
