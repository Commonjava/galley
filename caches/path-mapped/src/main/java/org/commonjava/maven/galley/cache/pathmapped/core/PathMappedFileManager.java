package org.commonjava.maven.galley.cache.pathmapped.core;

import org.commonjava.maven.galley.cache.pathmapped.config.PathMappedStorageConfig;
import org.commonjava.maven.galley.cache.pathmapped.model.PathKey;
import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;
import org.commonjava.maven.galley.cache.pathmapped.spi.PathDB;
import org.commonjava.maven.galley.cache.pathmapped.spi.PhysicalStore;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class PathMappedFileManager
{
    private final PathDB pathDB;

    private final PhysicalStore physicalStore;

    private final PathMappedStorageConfig config;

    public PathMappedFileManager( PathMappedStorageConfig config, PathDB pathDB, PhysicalStore physicalStore )
    {
        this.pathDB = pathDB;
        this.physicalStore = physicalStore;
        this.config = config;
    }

    public InputStream openInputStream( String fileSystem, String path ) throws IOException
    {
        String storageFile = pathDB.getStorageFile( fileSystem, path );
        if ( storageFile == null )
        {
            return null;
        }
        return physicalStore.getInputStream( storageFile );
    }

    public OutputStream openOutputStream( String fileSystem, String path ) throws IOException
    {
        FileInfo fileInfo = physicalStore.getFileInfo( fileSystem, path );
        return new PathDBOutputStream( pathDB, fileSystem, path, fileInfo, physicalStore.getOutputStream( fileInfo ) );
    }

    public boolean delete( String fileSystem, String path )
    {
        return pathDB.delete( fileSystem, path );
    }

    public void cleanupCurrentThread()
    {
    }

    public void startReporting()
    {
    }

    public void stopReporting()
    {
    }

    // append '/' to directory names
    public String[] list( String fileSystem, String path )
    {
        List<PathMap> paths = pathDB.list( fileSystem, path );
        return paths.stream().map( x -> {
            PathKey pk = x.getPathKey();
            String p = pk.getFilename();
            if ( x.getFileId() == null )
            {
                p += "/";
            }
            return p;
        } ).collect( Collectors.toList() ).toArray( new String[0] );
    }

    public int getFileLength( String fileSystem, String path )
    {
        return pathDB.getFileLength( fileSystem, path );
    }

    public long getFileLastModified( String fileSystem, String path )
    {
        return pathDB.getFileLastModified( fileSystem, path );
    }

    public boolean exists( String fileSystem, String path )
    {
        return pathDB.exists( fileSystem, path );
    }

    public boolean isDirectory( String fileSystem, String path )
    {
        return pathDB.isDirectory( fileSystem, path );
    }

    public boolean isFile( String fileSystem, String path )
    {
        return pathDB.isFile( fileSystem, path );
    }

    public void copy( String fromFileSystem, String fromPath, String toFileSystem, String toPath )
    {
        pathDB.copy( fromFileSystem, fromPath, toFileSystem, toPath );
    }

    public void makeDirs( String fileSystem, String path )
    {
        pathDB.makeDirs( fileSystem, path );
    }

    public String getFileStoragePath( String fileSystem, String path )
    {
        return pathDB.getStorageFile( fileSystem, path );
    }

}
