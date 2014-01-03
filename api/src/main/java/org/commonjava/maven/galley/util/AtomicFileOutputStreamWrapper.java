/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.commonjava.maven.galley.spi.cache.CacheProvider;

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
        this.downloadFile = new File( targetFile.getPath() + CacheProvider.SUFFIX_TO_DOWNLOAD );
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
