package org.commonjava.maven.galley.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AtomicFileOutputStreamWrapper
    extends OutputStream
{

    private final OutputStream stream;

    private final File downloadFile;

    private final File targetFile;

    public AtomicFileOutputStreamWrapper( final File targetFile )
        throws FileNotFoundException
    {
        this.targetFile = targetFile;
        this.downloadFile = new File( targetFile.getPath() + ".download" );
        this.stream = new FileOutputStream( downloadFile );
    }

    @Override
    public void write( final int b )
        throws IOException
    {
        stream.write( b );
    }

    @Override
    public void close()
        throws IOException
    {
        stream.close();
        downloadFile.renameTo( targetFile );
    }

    @Override
    public void write( final byte[] b )
        throws IOException
    {
        stream.write( b );
    }

    @Override
    public void write( final byte[] b, final int off, final int len )
        throws IOException
    {
        stream.write( b, off, len );
    }

    @Override
    public void flush()
        throws IOException
    {
        stream.flush();
    }

}
