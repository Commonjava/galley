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

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.auth.PasswordManager;

@Named( "attribute-galley-passwd" )
@Alternative
public class AttributePasswordManager
    implements PasswordManager
{

    private static final String PASSWORD_PREFIX = "password_";

    @Override
    public String getPassword( final PasswordEntry id )
    {
        final Location loc = id.getLocation();
        final String type = id.getPasswordType();
        return loc.getAttribute( PASSWORD_PREFIX + type, String.class );
    }

    public static void bind( final Location loc, final String type, final String password )
    {
        if ( password == null )
        {
            return;
        }

        loc.setAttribute( PASSWORD_PREFIX + type, password );
    }

    public static void bind( final PasswordEntry pwid, final String password )
    {
        if ( password == null )
        {
            return;
        }

        pwid.getLocation()
            .setAttribute( PASSWORD_PREFIX + pwid.getPasswordType(), password );
    }

}
