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

import java.util.Map;

public interface Location
{

    String CONNECTION_TIMEOUT_SECONDS = "connection-timeout";

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
