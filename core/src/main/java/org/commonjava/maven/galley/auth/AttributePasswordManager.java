/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
