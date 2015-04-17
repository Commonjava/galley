/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.auth;

import org.commonjava.maven.galley.model.Location;

public class PasswordEntry
{

    public static final String USER_PASSWORD = "userPassword";

    public static final String KEY_PASSWORD = "keyPassword";

    public static final String PROXY_PASSWORD = "proxyPassword";

    private final Location location;

    private final String passwordType;

    public PasswordEntry( final Location location, final String passwordType )
    {
        this.location = location;
        this.passwordType = passwordType;
    }

    public Location getLocation()
    {
        return location;
    }

    public String getPasswordType()
    {
        return passwordType;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( location == null ) ? 0 : location.hashCode() );
        result = prime * result + ( ( passwordType == null ) ? 0 : passwordType.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final PasswordEntry other = (PasswordEntry) obj;
        if ( location == null )
        {
            if ( other.location != null )
            {
                return false;
            }
        }
        else if ( !location.equals( other.location ) )
        {
            return false;
        }
        if ( passwordType == null )
        {
            if ( other.passwordType != null )
            {
                return false;
            }
        }
        else if ( !passwordType.equals( other.passwordType ) )
        {
            return false;
        }
        return true;
    }

}
