package org.commonjava.maven.galley.io.checksum;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

/**
 * Created by jdcasey on 5/5/17.
 */
public interface ChecksummingDecoratorAdvisor
{
    enum ChecksumAdvice
    {
        NO_DECORATE,
        CALCULATE_NO_WRITE,
        CALCULATE_AND_WRITE;
    }

    ChecksumAdvice getDecorationAdvice( Transfer transfer, TransferOperation operation, EventMetadata eventMetadata );
}
