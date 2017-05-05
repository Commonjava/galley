package org.commonjava.maven.galley.io.checksum;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

import static org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor.ChecksumAdvice.NO_DECORATE;

/**
 * Created by jdcasey on 5/5/17.
 */
public class DisabledChecksummingDecoratorAdvisor
        implements ChecksummingDecoratorAdvisor
{
    @Override
    public ChecksumAdvice getDecorationAdvice( final Transfer transfer, final TransferOperation operation,
                                               final EventMetadata eventMetadata )
    {
        return NO_DECORATE;
    }
}
