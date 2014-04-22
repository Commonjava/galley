/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.transport.htcli.testutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;

public class TestCacheProvider
    implements CacheProvider
{

    private final File dir;

    private final FileEventManager events;

    private final TransferDecorator decorator;

    public TestCacheProvider( final File dir, final FileEventManager events, final TransferDecorator decorator )
    {
        this.dir = dir;
        this.events = events;
        this.decorator = decorator;
    }

    public Transfer writeClasspathResourceToCache( final ConcreteResource resource, final String cpResource )
        throws IOException
    {
        final InputStream in = Thread.currentThread()
                                     .getContextClassLoader()
                                     .getResourceAsStream( cpResource );
        if ( in == null )
        {
            throw new IOException( "Classpath resource not found: " + cpResource );
        }

        final Transfer tx = getTransfer( resource );
        OutputStream out = null;
        try
        {
            out = tx.openOutputStream( TransferOperation.UPLOAD, false );
            IOUtils.copy( in, out );
        }
        finally
        {
            IOUtils.closeQuietly( in );
            IOUtils.closeQuietly( out );
        }

        return tx;
    }

    public Transfer writeToCache( final ConcreteResource resource, final String content )
        throws IOException
    {
        if ( content == null )
        {
            throw new IOException( "Content is empty!" );
        }

        final Transfer tx = getTransfer( resource );
        OutputStream out = null;
        try
        {
            out = tx.openOutputStream( TransferOperation.UPLOAD, false );
            out.write( content.getBytes() );
        }
        finally
        {
            IOUtils.closeQuietly( out );
        }

        return tx;
    }

    @Override
    public boolean isDirectory( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).isDirectory();
    }

    @Override
    public InputStream openInputStream( final ConcreteResource resource )
        throws IOException
    {
        return new FileInputStream( getDetachedFile( resource ) );
    }

    @Override
    public OutputStream openOutputStream( final ConcreteResource resource )
        throws IOException
    {
        final File f = getDetachedFile( resource );
        final File d = f.getParentFile();
        if ( d != null )
        {
            d.mkdirs();
        }

        return new FileOutputStream( f );
    }

    @Override
    public boolean exists( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).exists();
    }

    @Override
    public void copy( final ConcreteResource from, final ConcreteResource to )
        throws IOException
    {
        final File ff = getDetachedFile( from );
        final File tf = getDetachedFile( to );
        if ( ff.isDirectory() )
        {
            FileUtils.copyDirectory( ff, tf );
        }
        else
        {
            FileUtils.copyFile( ff, tf );
        }
    }

    @Override
    public String getFilePath( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).getPath();
    }

    @Override
    public boolean delete( final ConcreteResource resource )
        throws IOException
    {
        FileUtils.forceDelete( getDetachedFile( resource ) );
        return true;
    }

    @Override
    public String[] list( final ConcreteResource resource )
    {
        return getDetachedFile( resource ).list();
    }

    @Override
    public File getDetachedFile( final ConcreteResource resource )
    {
        return new File( new File( dir, resource.getLocationName() ), resource.getPath() );
    }

    @Override
    public void mkdirs( final ConcreteResource resource )
        throws IOException
    {
        getDetachedFile( resource ).mkdirs();
    }

    @Override
    public void createFile( final ConcreteResource resource )
        throws IOException
    {
        getDetachedFile( resource ).createNewFile();
    }

    @Override
    public void createAlias( final ConcreteResource from, final ConcreteResource to )
        throws IOException
    {
        final File fromFile = getDetachedFile( from );
        final File toFile = getDetachedFile( to );
        FileUtils.copyFile( fromFile, toFile );
        //        Files.createLink( Paths.get( fromFile.toURI() ), Paths.get( toFile.toURI() ) );
    }

    @Override
    public void clearTransferCache()
    {
    }

    @Override
    public Transfer getTransfer( final ConcreteResource resource )
    {
        return new Transfer( resource, this, events, decorator );
    }

}
