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
package org.commonjava.maven.galley.filearc.internal;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.join;
import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.getArchiveFile;
import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.isJar;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.ListingJob;

public class ZipListing
    implements ListingJob
{

    private TransferException error;

    private final ConcreteResource resource;

    private final Transfer target;

    public ZipListing( final ConcreteResource resource, final Transfer target )
    {
        this.resource = resource;
        this.target = target;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public ListingResult call()
    {
        final File src = getArchiveFile( resource.getLocationUri() );
        if ( !src.canRead() || src.isDirectory() )
        {
            return null;
        }

        final boolean isJar = isJar( resource.getLocationUri() );

        final TreeSet<String> filenames = new TreeSet<String>();

        ZipFile zf = null;
        try
        {
            if ( isJar )
            {
                zf = new JarFile( src );
            }
            else
            {
                zf = new ZipFile( src );
            }

            final String path = resource.getPath();
            final int pathLen = path.length();
            for ( final ZipEntry entry : Collections.list( zf.entries() ) )
            {
                String name = entry.getName();
                if ( name.startsWith( path ) )
                {
                    name = name.substring( pathLen );

                    if ( name.startsWith( "/" ) && name.length() > 1 )
                    {
                        name = name.substring( 1 );

                        if ( name.indexOf( "/" ) < 0 )
                        {
                            filenames.add( name );
                        }
                    }
                }
            }

        }
        catch ( final IOException e )
        {
            error = new TransferException( "Failed to get listing for: %s to: %s. Reason: %s", e, resource, e.getMessage() );
        }
        finally
        {
            if ( zf != null )
            {
                try
                {
                    zf.close();
                }
                catch ( final IOException e )
                {
                }
            }
        }

        if ( !filenames.isEmpty() )
        {
            OutputStream stream = null;
            try
            {
                stream = target.openOutputStream( TransferOperation.DOWNLOAD );
                stream.write( join( filenames, "\n" ).getBytes( "UTF-8" ) );

                return new ListingResult( resource, filenames.toArray( new String[filenames.size()] ) );
            }
            catch ( final IOException e )
            {
                error = new TransferException( "Failed to write listing to: %s. Reason: %s", e, target, e.getMessage() );
            }
            finally
            {
                closeQuietly( stream );
            }
        }

        return null;
    }

}
