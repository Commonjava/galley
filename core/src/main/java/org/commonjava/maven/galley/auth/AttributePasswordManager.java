package org.commonjava.maven.galley.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.auth.PasswordManager;

@ApplicationScoped
@Named( "attribute" )
public class AttributePasswordManager
    implements PasswordManager
{

    private static final String PASSWORD_PREFIX = "password_";

    @Override
    public String getPassword( final PasswordIdentifier id )
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

    public static void bind( final PasswordIdentifier pwid, final String password )
    {
        if ( password == null )
        {
            return;
        }

        pwid.getLocation()
            .setAttribute( PASSWORD_PREFIX + pwid.getPasswordType(), password );
    }

}
