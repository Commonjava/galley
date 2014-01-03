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
