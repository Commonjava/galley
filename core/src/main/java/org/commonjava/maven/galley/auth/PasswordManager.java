package org.commonjava.maven.galley.auth;

import org.commonjava.maven.galley.model.Location;

public interface PasswordManager
{

    String getPassword( Location location, String passwordType );

}
