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
