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
