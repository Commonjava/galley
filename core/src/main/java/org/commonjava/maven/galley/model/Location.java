package org.commonjava.maven.galley.model;

public interface Location
{

    int DEFAULT_TIMEOUT_SECONDS = 30;

    boolean allowsSnapshots();

    boolean allowsReleases();

    String getUri();

    int getTimeoutSeconds();

}
