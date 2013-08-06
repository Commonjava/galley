package org.commonjava.maven.galley.auth;

import java.util.HashMap;
import java.util.Map;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.auth.PasswordManager;

public class MemoryPasswordManager
    implements PasswordManager
{

    private final Map<PasswordIdentifier, String> passwords = new HashMap<>();

    public void setPasswordFor( final String password, final Location loc, final String type )
    {
        passwords.put( new PasswordIdentifier( loc, type ), password );
    }

    public void setPasswordFor( final String password, final PasswordIdentifier id )
    {
        passwords.put( id, password );
    }

    @Override
    public String getPassword( final PasswordIdentifier id )
    {
        return passwords.get( id );
    }

}
