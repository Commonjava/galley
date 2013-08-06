package org.commonjava.maven.galley.spi.auth;

import org.commonjava.maven.galley.auth.PasswordIdentifier;


public interface PasswordManager
{

    String getPassword( PasswordIdentifier id );

}
