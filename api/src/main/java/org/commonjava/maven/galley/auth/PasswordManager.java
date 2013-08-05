package org.commonjava.maven.galley.auth;


public interface PasswordManager
{

    String getPassword( PasswordIdentifier id );

}
