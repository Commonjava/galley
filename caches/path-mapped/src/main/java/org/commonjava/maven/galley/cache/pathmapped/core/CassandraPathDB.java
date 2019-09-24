package org.commonjava.maven.galley.cache.pathmapped.core;

import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;
import org.commonjava.maven.galley.cache.pathmapped.spi.PathDB;

import java.util.List;

public class CassandraPathDB
                implements PathDB
{
    public List<PathMap> list( String fileSystem, String path )
    {
        return null;
    }

    @Override
    public int getFileLength( String fileSystem, String path )
    {
        return 0;
    }

    @Override
    public long getFileLastModified( String fileSystem, String path )
    {
        return 0;
    }

    @Override
    public boolean exists( String fileSystem, String path )
    {
        return false;
    }

    @Override
    public void insert( PathMap pathMap )
    {

    }

    @Override
    public boolean isDirectory( String fileSystem, String path )
    {
        return false;
    }

    @Override
    public boolean isFile( String fileSystem, String path )
    {
        return false;
    }

    @Override
    public boolean delete( String fileSystem, String path )
    {
        return false;
    }

    @Override
    public String getStorageFile( String fileSystem, String path )
    {
        return null;
    }

    @Override
    public void copy( String fromFileSystem, String fromPath, String toFileSystem, String toPath )
    {

    }

    @Override
    public void makeDirs( String fileSystem, String path )
    {

    }
}
