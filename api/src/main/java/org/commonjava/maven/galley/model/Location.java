package org.commonjava.maven.galley.model;

import java.util.Map;

public interface Location
{

    String CONNECTION_TIMEOUT_SECONDS = "connection-timeout";

    int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30;

    int DEFAULT_CACHE_TIMEOUT_SECONDS = 86400;

    boolean allowsDownloading();

    boolean allowsPublishing();

    boolean allowsStoring();

    boolean allowsSnapshots();

    boolean allowsReleases();

    String getUri();

    String getName();

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
