package org.commonjava.maven.galley.cache.pathmapped.core;

import org.commonjava.maven.galley.cache.pathmapped.model.PathKey;
import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;
import org.commonjava.maven.galley.cache.pathmapped.spi.PathDB;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getPathKey;

public class PathDBOutputStream
                extends FilterOutputStream
{
    private final PathDB pathDB;

    private final String fileSystem;

    private final String path;

    private final String fileId;

    private final String fileStorage;

    private int size;

    public PathDBOutputStream( PathDB pathDB, String fileSystem, String path, FileInfo fileInfo, OutputStream out )
    {
        super( out );
        this.pathDB = pathDB;
        this.fileSystem = fileSystem;
        this.path = path;
        this.fileId = fileInfo.getFileId();
        this.fileStorage = fileInfo.getFileStorage();
    }

    @Override
    public void write( int b ) throws IOException
    {
        super.write( b );
        size += 1;
    }

    @Override
    public void close() throws IOException
    {
        PathMap pathMap = new PathMap();

        pathMap.setPathKey( getPathKey( fileSystem, path ) );
        pathMap.setCreation( new Date() );
        pathMap.setFileId( fileId );
        pathMap.setFileStorage( fileStorage );
        pathMap.setSize( size );

        pathDB.insert( pathMap );
    }
}
