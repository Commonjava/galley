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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.auth.PasswordManager;

@Named( "memory-galley-passwd" )
@Alternative
public class MemoryPasswordManager
    implements PasswordManager
{

    private final Map<PasswordEntry, String> passwords = new HashMap<PasswordEntry, String>();

    public void setPasswordFor( final String password, final Location loc, final String type )
    {
        passwords.put( new PasswordEntry( loc, type ), password );
    }

    public void setPasswordFor( final String password, final PasswordEntry id )
    {
        passwords.put( id, password );
    }

    @Override
    public String getPassword( final PasswordEntry id )
    {
        return passwords.get( id );
    }

}
