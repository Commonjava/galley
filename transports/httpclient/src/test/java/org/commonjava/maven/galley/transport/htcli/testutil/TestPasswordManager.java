package org.commonjava.maven.galley.transport.htcli.testutil;

import java.util.HashMap;
import java.util.Map;

import org.commonjava.maven.galley.auth.PasswordEntry;
import org.commonjava.maven.galley.spi.auth.PasswordManager;

public class TestPasswordManager
    implements PasswordManager
{

    private final Map<PasswordEntry, String> passwords = new HashMap<>();

    @Override
    public String getPassword( final PasswordEntry id )
    {
        return passwords.get( id );
    }

    public void setPassword( final PasswordEntry id, final String password )
    {
        passwords.put( id, password );
    }

}
