package org.commonjava.maven.galley.model;

import java.util.Map;

public interface Location
{

    int DEFAULT_TIMEOUT_SECONDS = 30;

    boolean allowsDownloading();

    boolean allowsPublishing();

    boolean allowsStoring();

    boolean allowsSnapshots();

    boolean allowsReleases();

    String getUri();

    int getTimeoutSeconds();

    /**
     * Reminder that equality checks are important here!
     */
    @Override
    boolean equals( Object other );

    /**
     * Reminder that equality checks are important here!
     */
    @Override
    int hashCode();

    Map<String, Object> getAttributes();

    <T> T getAttribute( String key, Class<T> type );

    Object removeAttribute( String key );

    Object setAttribute( String key, Object value );

}
