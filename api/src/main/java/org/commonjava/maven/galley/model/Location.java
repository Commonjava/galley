/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.model;

import java.util.Map;

public interface Location
{

    String CONNECTION_TIMEOUT_SECONDS = "connection-timeout";

    String ATTR_ALT_STORAGE_LOCATION = "alt-storage-location";

    int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30;

    int DEFAULT_CACHE_TIMEOUT_SECONDS = 86400;

    int MIN_CACHE_TIMEOUT_SECONDS = 3600;

    boolean allowsDownloading();

    boolean allowsPublishing();

    boolean allowsStoring();

    boolean allowsSnapshots();

    boolean allowsReleases();

    String getUri();

    String getName();

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

    <T> T getAttribute( String key, Class<T> type, T defaultValue );

    Object removeAttribute( String key );

    Object setAttribute( String key, Object value );

}
