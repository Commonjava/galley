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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;

public class ZipExistence
    extends AbstractZipOperation
    implements ExistenceJob
{

    private TransferException error;

    public ZipExistence( final ConcreteResource resource )
    {
        super( resource );
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Boolean call()
    {
        final File src = getZipFile();
        if ( src.isDirectory() )
        {
            return null;
        }

        ZipFile zf = null;
        try
        {
            if ( isJarOperation() )
            {
                zf = new JarFile( src );
            }
            else
            {
                zf = new ZipFile( src );
            }

            final String path = getFullPath();
            logger.debug( "Looking for entry: {}", path );

            boolean found = false;
            for ( final ZipEntry entry : Collections.list( zf.entries() ) )
            {
                final String name = entry.getName();
                logger.debug( "Checking entry: {}", name );
                if ( name.equals( path ) )
                {
                    found = true;
                    break;
                }
            }

            return found;
        }
        catch ( final IOException e )
        {
            error =
                new TransferException( "Failed to get listing for: %s:%s to: %s. Reason: %s", e, getLocation(),
                                       getPath(), e.getMessage() );
        }
        finally
        {
            if ( zf != null )
            {
                //noinspection EmptyCatchBlock
                try
                {
                    zf.close();
                }
                catch ( final IOException e )
                {
                }
            }
        }

        return false;
    }

}
