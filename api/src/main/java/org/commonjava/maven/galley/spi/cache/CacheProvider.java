package org.commonjava.maven.galley.spi.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.Resource;

public interface CacheProvider
{

    boolean isDirectory( Resource resource );

    InputStream openInputStream( Resource resource )
        throws IOException;

    OutputStream openOutputStream( Resource resource )
        throws IOException;

    boolean exists( Resource resource );

    void copy( Resource from, Resource to )
        throws IOException;

    String getFilePath( Resource resource );

    boolean delete( Resource resource )
        throws IOException;

    String[] list( Resource resource );

    File getDetachedFile( Resource resource );

    void mkdirs( Resource resource )
        throws IOException;

    void createFile( Resource resource )
        throws IOException;

    void createAlias( Resource to, Resource from )
        throws IOException;

}
