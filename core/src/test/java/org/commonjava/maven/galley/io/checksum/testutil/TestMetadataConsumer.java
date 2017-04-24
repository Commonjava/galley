package org.commonjava.maven.galley.io.checksum.testutil;

import org.commonjava.maven.galley.io.checksum.TransferMetadata;
import org.commonjava.maven.galley.io.checksum.TransferMetadataConsumer;
import org.commonjava.maven.galley.model.Transfer;

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
        return metadata.get( transfer );
    }

    @Override
    public synchronized void addMetadata( final Transfer transfer, final TransferMetadata transferData )
    {
        metadata.put( transfer, transferData );
    }

    @Override
    public synchronized void removeMetadata( final Transfer transfer )
    {
        metadata.remove( transfer );
    }
}
