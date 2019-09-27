package org.commonjava.maven.galley.cache.pathmapped.core;

import org.apache.commons.codec.digest.DigestUtils;
import org.commonjava.maven.galley.cache.pathmapped.spi.PhysicalStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getStoragePathByFileId;

public class FileBasedPhysicalStore implements PhysicalStore
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final File baseDir;

    public FileBasedPhysicalStore( File baseDir )
    {
        this.baseDir = baseDir;
    }

    @Override
    public FileInfo getFileInfo( String fileSystem, String path )
    {
        String uri = fileSystem + ":" + path;
        String id = DigestUtils.md5Hex( uri );

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId( id );
        fileInfo.setFileStorage( getStoragePathByFileId( id ) );
        return fileInfo;
    }

    @Override
    public OutputStream getOutputStream( FileInfo fileInfo ) throws IOException
    {
        File f = new File( baseDir, fileInfo.getFileStorage() );
        File dir = f.getParentFile();
        if ( !dir.isDirectory() )
        {
            dir.mkdirs();
        }
        return new FileOutputStream( f );
    }

    @Override
    public InputStream getInputStream( String storageFile ) throws IOException
    {
        return new FileInputStream( new File( baseDir, storageFile ) );
    }

    @Override
    public boolean delete( FileInfo fileInfo )
    {
        File f = new File( baseDir, fileInfo.getFileStorage() );
        try
        {
            Files.deleteIfExists( Paths.get( f.getAbsolutePath() ) );
        }
        catch ( IOException e )
        {
            logger.error( "Failed to delete file: " + fileInfo, e );
            return false;
        }
        return true;
    }

}
