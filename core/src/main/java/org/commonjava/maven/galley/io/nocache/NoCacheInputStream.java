/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.io.nocache;

import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.util.IdempotentCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.commonjava.maven.galley.io.SpecialPathConstants.HTTP_METADATA_EXT;

/**
 * Created by ruhan on 4/25/17.
 */
public class NoCacheInputStream
        extends IdempotentCloseInputStream
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Transfer transfer;

    public NoCacheInputStream( final InputStream stream, final Transfer transfer )
    {
        super( stream );
        this.transfer = transfer;
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            logger.trace( "START CLOSE: {}", transfer );
            super.close();

            logger.trace( "Delete: {} and its siblings in: {}.", transfer.getPath(), transfer.getLocation() );
            transfer.delete( false );

            Transfer meta = transfer.getSibling( HTTP_METADATA_EXT );
            if ( meta != null && meta.exists() )
            {
                meta.delete( false );
            }
        }
        finally
        {
            logger.trace( "END CLOSE: {}", transfer );
        }
    }
}
