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
package org.commonjava.maven.galley.io.checksum.testutil;

import org.commonjava.maven.galley.io.checksum.TransferMetadata;
import org.commonjava.maven.galley.io.checksum.TransferMetadataConsumer;
import org.commonjava.maven.galley.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jdcasey on 4/24/17.
 */
public class TestMetadataConsumer
        implements TransferMetadataConsumer
{
    private Map<Transfer, TransferMetadata> metadata = new HashMap<>();

    public TransferMetadata getMetadata( Transfer transfer )
    {
        TransferMetadata transferMetadata = metadata.get( transfer );
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Returning metadata: {} for transfer: {}", transferMetadata, transfer );
        return transferMetadata;
    }

    @Override
    public boolean needsMetadataFor( final Transfer transfer )
    {
        return true;
    }

    @Override
    public synchronized void addMetadata( final Transfer transfer, final TransferMetadata transferData )
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Adding metadata: {} for transfer: {}", transferData, transfer );
        metadata.put( transfer, transferData );
    }

    @Override
    public synchronized void removeMetadata( final Transfer transfer )
    {
        metadata.remove( transfer );
    }
}
