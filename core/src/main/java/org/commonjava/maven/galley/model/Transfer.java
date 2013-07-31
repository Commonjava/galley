package org.commonjava.maven.galley.model;

import java.io.IOException;

public interface Transfer
{

    boolean exists();

    boolean isDirectory();

    String[] list();

    Transfer getChild( String sub );

    boolean delete()
        throws IOException;

}
