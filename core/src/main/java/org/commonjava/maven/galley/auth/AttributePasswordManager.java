package org.commonjava.maven.galley.auth;

import org.commonjava.maven.galley.model.Location;

public class AttributePasswordManager
    implements PasswordManager
{

    private static final String PASSWORD_PREFIX = "password_";

    public void setPasswordFor( final String password, final Location loc, final String type )
    {
        loc.setAttribute( PASSWORD_PREFIX + type, password );
    }

    @Override
    public String getPassword( final PasswordIdentifier id )
    {
        final Location loc = id.getLocation();
        final String type = id.getPasswordType();
        return loc.getAttribute( PASSWORD_PREFIX + type, String.class );
    }

}
