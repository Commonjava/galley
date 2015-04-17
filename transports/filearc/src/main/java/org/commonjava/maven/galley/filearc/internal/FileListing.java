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

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.ListingResult;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.commonjava.maven.galley.spi.transport.ListingJob;

public class FileListing
    implements ListingJob
{

    private TransferException error;

    private final File src;

    private final ConcreteResource resource;

    private final Transfer target;

    public FileListing( final ConcreteResource resource, final File src, final Transfer target )
    {
        this.resource = resource;
        this.src = src;
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
        if ( src.canRead() && src.isDirectory() )
        {
            final String[] raw = src.list();
            OutputStream stream = null;
            try
            {
                stream = target.openOutputStream( TransferOperation.DOWNLOAD );
                stream.write( join( raw, "\n" ).getBytes( "UTF-8" ) );

                return new ListingResult( resource, raw );
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
