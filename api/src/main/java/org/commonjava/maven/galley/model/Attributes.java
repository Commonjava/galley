/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.model;

/**
 * Wrapper to standardise API for some common attribute storage. Delegates
 * to implementing attribute class (e.g. {@link org.commonjava.maven.galley.model.ConcreteResource})
 */
public abstract class Attributes
{
    public static final String CONNECTION_TIMEOUT_SECONDS = "connection-timeout";
    public static final String ATTR_ALT_STORAGE_LOCATION = "alt-storage-location";

    public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30;
    public static final int DEFAULT_CACHE_TIMEOUT_SECONDS = 86400;
    public static final int MIN_CACHE_TIMEOUT_SECONDS = 3600;

    abstract public <T> T getAttribute( final String key, final Class<T> type );

    public int getTimeoutSeconds()
    {
        Integer timeoutSeconds = getAttribute( Attributes.CONNECTION_TIMEOUT_SECONDS, Integer.class );

        if ( timeoutSeconds == null )
        {
            timeoutSeconds = Attributes.DEFAULT_CONNECTION_TIMEOUT_SECONDS;
        }

        return timeoutSeconds;
    }

    public String getAltStorageLocation()
    {
        return getAttribute( Attributes.ATTR_ALT_STORAGE_LOCATION, String.class );
    }
}
