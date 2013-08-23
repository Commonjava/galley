package org.commonjava.maven.galley.spi.auth;

import org.commonjava.maven.galley.auth.PasswordEntry;


public interface PasswordManager
{

    String getPassword( PasswordEntry id );

}
