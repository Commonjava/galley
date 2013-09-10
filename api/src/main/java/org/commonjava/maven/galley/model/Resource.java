package org.commonjava.maven.galley.model;

public interface Resource
{

    String getPath();

    @Override
    int hashCode();

    @Override
    boolean equals( Object obj );

    boolean isRoot();

    Resource getParent();

    Resource getChild( String file );

    boolean allowsDownloading();

    boolean allowsPublishing();

    boolean allowsStoring();

    boolean allowsSnapshots();

    boolean allowsReleases();

}