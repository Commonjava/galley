package org.commonjava.maven.galley.spi.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.ConcreteResource;

public interface CacheProvider
{

    boolean isDirectory( ConcreteResource resource );

    InputStream openInputStream( ConcreteResource resource )
        throws IOException;

    OutputStream openOutputStream( ConcreteResource resource )
        throws IOException;

    boolean exists( ConcreteResource resource );

    void copy( ConcreteResource from, ConcreteResource to )
        throws IOException;

    String getFilePath( ConcreteResource resource );

    boolean delete( ConcreteResource resource )
        throws IOException;

    String[] list( ConcreteResource resource );

    File getDetachedFile( ConcreteResource resource );

    void mkdirs( ConcreteResource resource )
        throws IOException;

    void createFile( ConcreteResource resource )
        throws IOException;

    void createAlias( ConcreteResource from, ConcreteResource to )
        throws IOException;

}
