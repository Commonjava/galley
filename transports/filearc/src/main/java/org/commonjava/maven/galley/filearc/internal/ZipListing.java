/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.filearc.internal;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.join;

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
    extends AbstractZipOperation
    implements ListingJob
{

    private TransferException error;

    public ZipListing( final ConcreteResource resource, final Transfer target )
    {
        super( resource );
        this.transfer = target;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public ListingResult call()
    {
        final File src = getZipFile();
        if ( !src.canRead() || src.isDirectory() )
        {
            return null;
        }

        final boolean isJar = isJarOperation();

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

            final String path = getFullPath();
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
                stream = transfer.openOutputStream( TransferOperation.DOWNLOAD );
                stream.write( join( filenames, "\n" ).getBytes( "UTF-8" ) );

                return new ListingResult( resource, filenames.toArray( new String[filenames.size()] ) );
            }
            catch ( final IOException e )
            {
                error =
                    new TransferException( "Failed to write listing to: %s. Reason: %s", e, transfer, e.getMessage() );
            }
            finally
            {
                closeQuietly( stream );
            }
        }

        return null;
    }

}
