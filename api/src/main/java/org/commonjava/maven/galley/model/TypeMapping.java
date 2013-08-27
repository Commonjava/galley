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
