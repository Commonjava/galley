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

public interface Location
{
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

    <T> T getAttribute( String key, Class<T> type );

    Object removeAttribute( String key );

    Object setAttribute( String key, Object value );

}
