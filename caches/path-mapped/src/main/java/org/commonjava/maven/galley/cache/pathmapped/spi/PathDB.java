package org.commonjava.maven.galley.cache.pathmapped.spi;

import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;
import org.commonjava.maven.galley.cache.pathmapped.model.Reclaim;

import java.util.Date;
import java.util.List;

public interface PathDB
{
    List<PathMap> list( String fileSystem, String path );

    int getFileLength( String fileSystem, String path );

    long getFileLastModified( String fileSystem, String path );

    boolean exists( String fileSystem, String path );

    void insert( String fileSystem, String path, Date date, String fileId, int size, String fileStorage );

    void insert( PathMap pathMap );

    boolean isDirectory( String fileSystem, String path );

    boolean isFile( String fileSystem, String path );

    boolean delete( String fileSystem, String path );

    String getStorageFile( String fileSystem, String path );

    boolean copy( String fromFileSystem, String fromPath, String toFileSystem, String toPath );

    void makeDirs( String fileSystem, String path );

    List<Reclaim> listOrphanedFiles();

    void removeFromReclaim( Reclaim reclaim );
}
