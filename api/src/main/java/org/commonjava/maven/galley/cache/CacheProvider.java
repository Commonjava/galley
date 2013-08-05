package org.commonjava.maven.galley.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.Location;

public interface CacheProvider
{

    boolean isDirectory( Location loc, String path );

    InputStream openInputStream( Location loc, String path )
        throws IOException;

    OutputStream openOutputStream( Location loc, String path )
        throws IOException;

    boolean exists( Location loc, String path );

    void copy( Location fromKey, String fromPath, Location toKey, String toPath )
        throws IOException;

    String getFilePath( Location loc, String path );

    boolean delete( Location loc, String path )
        throws IOException;

    String[] list( Location loc, String path );

    File getDetachedFile( Location loc, String path );

    void mkdirs( Location loc, String path )
        throws IOException;

    void createFile( Location loc, String path )
        throws IOException;

    void createAlias( Location toKey, String toPath, Location fromKey, String fromPath )
        throws IOException;

}
